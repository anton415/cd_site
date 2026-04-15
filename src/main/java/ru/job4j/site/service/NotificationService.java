package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.*;
import ru.job4j.site.util.RestAuthCall;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final EurekaUriProvider uriProvider;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String SERVICE_ID = "notification";
    private static final String TOPIC_NEW_INTERVIEW = "notification.new-interview";
    private static final String TOPIC_INNER_MESSAGE = "notification.inner-message";
    private static final String TOPIC_FEEDBACK = "notification.feedback";
    private static final String TOPIC_SUBSCRIBE_TOPIC = "notification.subscribe-topic";
    private static final String TOPIC_PARTICIPATE = "notification.participate";
    private static final String TOPIC_CANCEL_INTERVIEW = "notification.cancel-interview";
    private static final String TOPIC_PARTICIPANT_DISMISSED = "notification.participant-dismissed";
    private static final String TOPIC_APPROVED_WISHER = "notification.approved-wisher";

    public NotificationService(EurekaUriProvider uriProvider,
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.uriProvider = uriProvider;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void addSubscribeCategory(String token, int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        var mapper = new ObjectMapper();
        try {
            var url = String
                    .format("%s/subscribeCategory/add", uriProvider.getUri(SERVICE_ID));
            new RestAuthCall(url).post(token, mapper.writeValueAsString(subscribeCategory));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
        }
    }

    public void deleteSubscribeCategory(String token, int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        var mapper = new ObjectMapper();
        try {
            var url = String
                    .format("%s/subscribeCategory/delete", uriProvider.getUri(SERVICE_ID));
            new RestAuthCall(url).post(token, mapper.writeValueAsString(subscribeCategory));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
        }
    }

    public Optional<UserDTO> findCategoriesByUserId(int id) {
        var mapper = new ObjectMapper();
        try {
            var text = new RestAuthCall(String
                    .format("%s/subscribeCategory/%d", uriProvider.getUri(SERVICE_ID), id))
                    .get();
            List<Integer> list = mapper.readValue(text, new TypeReference<>() {
            });
            return Optional.of(new UserDTO(id, list));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void addSubscribeTopic(String token, int userId, int topicId) {
        SubscribeTopicDTO subscribeTopicDTO = new SubscribeTopicDTO(userId, topicId);
        var mapper = new ObjectMapper();
        try {
            var url = String.format("%s/subscribeTopic/add", uriProvider.getUri(SERVICE_ID));
            new RestAuthCall(url).post(
                    token, mapper.writeValueAsString(subscribeTopicDTO));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
        }
    }

    public void deleteSubscribeTopic(String token, int userId, int topicId) {
        SubscribeTopicDTO subscribeTopic = new SubscribeTopicDTO(userId, topicId);
        var mapper = new ObjectMapper();
        try {
            var url = String
                    .format("%s/subscribeTopic/delete", uriProvider.getUri(SERVICE_ID));
            new RestAuthCall(url).post(
                    token, mapper.writeValueAsString(subscribeTopic));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
        }
    }

    public Optional<UserTopicDTO> findTopicByUserId(int id) {
        var mapper = new ObjectMapper();
        try {
            var text = new RestAuthCall(String
                    .format("%s/subscribeTopic/%d", uriProvider.getUri(SERVICE_ID), id))
                    .get();
            List<Integer> list = mapper.readValue(text, new TypeReference<>() {
            });
            return Optional.of(new UserTopicDTO(id, list));
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<InnerMessageDTO> findBotMessageByUserId(String token, int id) {
        var url = String
                .format("%s/messages/actual/%d", uriProvider.getUri(SERVICE_ID), id);
        var mapper = new ObjectMapper();
        try {
            var text = new RestAuthCall(url).get(token);
            return mapper.readValue(text, new TypeReference<>() {
            });
        } catch (Exception e) {
            LOG.error("API notification not found, error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void notifyAboutInterviewCreation(String token,
                                             CategoryWithTopicDTO categoryAndTopicIds) {
        try {
            sendKafkaEvent(TOPIC_NEW_INTERVIEW, categoryAndTopicIds);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    public void sendFeedBackMessage(String token, InnerMessageDTO innerMessage) {
        try {
            sendKafkaEvent(TOPIC_INNER_MESSAGE, innerMessage);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    public void sendFeedbackNotification(String token,
                                         FeedbackNotificationDTO feedbackNotification) {
        try {
            sendKafkaEvent(TOPIC_FEEDBACK, feedbackNotification);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    /**
     * Метод отправляет запрос в сервис Notification.
     * Запрос для отправки подписчикам темы о том, что появилось новое интервью.
     *
     * @param token              String
     * @param interviewNotifyDTO InterviewNotifyDTO
     * @throws JsonProcessingException Exception
     */
    public void sendSubscribeTopic(String token, InterviewNotifyDTO interviewNotifyDTO) {
        try {
            sendKafkaEvent(TOPIC_SUBSCRIBE_TOPIC, interviewNotifyDTO);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    /**
     * Метод оправляет запрос в сервис Notification.
     * Запрос для отправки автору собеседования о том что добавился участник.
     *
     * @param token           String
     * @param wisherNotifyDTO WisherNotifyDTO
     */
    public void sendParticipateAuthor(String token, WisherNotifyDTO wisherNotifyDTO) {
        try {
            sendKafkaEvent(TOPIC_PARTICIPATE, wisherNotifyDTO);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    /**
     * Метод оправляет запрос в сервис Notification.
     * Запрос для отправки сообщения участнику собеседования о том что автор собеседования
     * удалил собеседование.
     *
     * @param token              String
     * @param cancelInterviewDTO CancelInterviewNotificationDTO
     */
    public void sendParticipateCancelInterview(String token,
                                               CancelInterviewNotificationDTO cancelInterviewDTO) {
        try {
            sendKafkaEvent(TOPIC_CANCEL_INTERVIEW, cancelInterviewDTO);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent", e);
        }
    }

    /**
     * Метод оправляет запрос в сервис Notification.
     * Запрос для отправки сообщения участнику собеседования о том что автор собеседования
     * одобрил другого участника.
     *
     * @param token                  String
     * @param wisherDismissedDTOList List<WisherDismissedDTO>
     */
    public void sendParticipantIsDismissed(String token,
                                           List<WisherDismissedDTO> wisherDismissedDTOList) {
        try {
            sendKafkaEvent(TOPIC_PARTICIPANT_DISMISSED, wisherDismissedDTOList);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent", e);
        }
    }

    public void approvedWisher(String token, WisherApprovedDTO wisherApprovedDTO) {
        try {
            sendKafkaEvent(TOPIC_APPROVED_WISHER, wisherApprovedDTO);
        } catch (Exception e) {
            LOG.error("Kafka notification event not sent, error: {}", e.getMessage());
        }
    }

    /**
     * Метод оправляет запрос в сервис Notification.
     * @param topic String
     * @param payload Object
     * @throws JsonProcessingException Exception
     */
    private void sendKafkaEvent(String topic, Object payload) throws JsonProcessingException {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(payload));
    }
}
