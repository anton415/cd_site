package ru.job4j.site.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.job4j.site.dto.InterviewNotifyDTO;
import ru.job4j.site.dto.WisherDismissedDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EurekaUriProvider uriProvider;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new NotificationService(uriProvider, kafkaTemplate, objectMapper);
    }

    @Test
    @DisplayName("При отправке уведомления о новой теме, должен публиковаться JSON в Kafka")
    void whenSendSubscribeTopicThenPublishJsonToKafka() throws Exception {
        InterviewNotifyDTO dto = InterviewNotifyDTO.of()
                .id(1)
                .submitterId(2)
                .title("Java Interview")
                .topicId(3)
                .topicName("Java")
                .categoryId(4)
                .categoryName("Backend")
                .build();
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        service.sendSubscribeTopic("token", dto);

        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());
        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertThat(topicCaptor.getValue()).isEqualTo("notification.subscribe-topic");
        assertThat(payload.get("id").asInt()).isEqualTo(dto.getId());
        assertThat(payload.get("topicName").asText()).isEqualTo(dto.getTopicName());
        assertThat(payload.get("categoryName").asText()).isEqualTo(dto.getCategoryName());
    }

    @Test
    @DisplayName("При отправке уведомления об отклонении участника, должен публиковаться JSON в Kafka")
    void whenSendParticipantIsDismissedThenPublishListJsonToKafka() throws Exception {
        List<WisherDismissedDTO> dismissed = List.of(
                WisherDismissedDTO.of()
                        .interviewId(10)
                        .interviewTitle("System Design")
                        .submitterId(11)
                        .submitterName("Anton")
                        .userId(12)
                        .build()
        );
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        service.sendParticipantIsDismissed("token", dismissed);

        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());
        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertThat(topicCaptor.getValue()).isEqualTo("notification.participant-dismissed");
        assertThat(payload).hasSize(1);
        assertThat(payload.get(0).get("userId").asInt()).isEqualTo(12);
        assertThat(payload.get(0).get("submitterName").asText()).isEqualTo("Anton");
    }
}
