package ru.job4j.site.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.TopicLiteDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CategoriesServiceTest {
    private final TopicsService topicsService = mock(TopicsService.class);
    private final InterviewsService interviewsService = mock(InterviewsService.class);
    private final EurekaUriProvider uriProvider = mock(EurekaUriProvider.class);
    private final CategoriesService categoriesService =
            spy(new CategoriesService(topicsService, interviewsService, uriProvider));

    @Test
    @DisplayName("Проверяем, что сервис инициализирован корректно")
    void injectedNotNull() {
        assertEquals(categoriesService, categoriesService, "Сервис должен быть инициализирован корректно и не быть null");
    }

    @Test
    @DisplayName("Проверяем, что метод возвращает самые популярные категории")
    void whenGetMostPopularThenCountNewInterviewsByCategory() throws Exception {
        var javaBase = new CategoryDTO(1, "Java Base");
        var javaCore = new CategoryDTO(2, "Java Core");
        var spring = new CategoryDTO(3, "Spring");
        doReturn(List.of(javaBase, javaCore, spring)).when(categoriesService).getPopularFromDesc();
        when(topicsService.getAllTopicLiteDTO()).thenReturn(List.of(
                new TopicLiteDTO(11, "Базовый синтаксис", "", 1, "Java Base", 1),
                new TopicLiteDTO(12, "ООП", "", 1, "Java Base", 2),
                new TopicLiteDTO(21, "Структуры данных и алгоритмы", "", 2, "Java Core", 1),
                new TopicLiteDTO(31, "MVC", "", 3, "Spring", 1)
        ));
        when(interviewsService.getNewInterviews()).thenReturn(List.of(
                interview(1001, 11),
                interview(1002, 12),
                interview(1003, 21)
        ));

        var actual = categoriesService.getMostPopular();

        assertEquals(2L, actual.getFirst().getCountInterview(), "Количество собеседований для первой категории должно быть равно 2");
}

    @Test
    @DisplayName("Проверяем, что метод возвращает все категории с учетом тем")
    void whenGetAllWithTopicsThenCategoryWithoutInterviewGetsZero() throws Exception {
        var javaBase = new CategoryDTO(1, "Java Base");
        var spring = new CategoryDTO(3, "Spring");
        doReturn(List.of(javaBase, spring)).when(categoriesService).getAll();
        when(topicsService.getAllTopicLiteDTO()).thenReturn(List.of(
                new TopicLiteDTO(11, "Базовый синтаксис", "", 1, "Java Base", 1),
                new TopicLiteDTO(31, "MVC", "", 3, "Spring", 1)
        ));
        when(interviewsService.getNewInterviews()).thenReturn(List.of(interview(1001, 11)));

        var actual = categoriesService.getAllWithTopics();

        assertEquals(1L, actual.getFirst().getCountInterview(), "Количество собеседований для первой категории должно быть равно 1");
    }

    @Test
    @DisplayName("Проверяем, что метод игнорирует собеседования с неизвестной темой")   
    void whenInterviewHasUnknownTopicThenIgnoreIt() throws Exception {
        var javaBase = new CategoryDTO(1, "Java Base");
        doReturn(List.of(javaBase)).when(categoriesService).getPopularFromDesc();
        when(topicsService.getAllTopicLiteDTO()).thenReturn(List.of(
                new TopicLiteDTO(11, "Базовый синтаксис", "", 1, "Java Base", 1)
        ));
        when(interviewsService.getNewInterviews()).thenReturn(List.of(
                interview(1001, 11),
                interview(1002, 999)
        ));

        var actual = categoriesService.getMostPopular();

        assertEquals(1L, actual.getFirst().getCountInterview(), "Количество собеседований для категории должно быть равно 1, так как второе собеседование с неизвестной темой должно быть проигнорировано");
    }

    /**
     * Вспомогательный метод для создания объекта InterviewDTO с заданными id и topicId.
     * @param id ID собеседования
     * @param topicId ID темы собеседования
     * @return InterviewDTO с заданными id и topicId
     */
    private InterviewDTO interview(int id, int topicId) {
        var interview = new InterviewDTO();
        interview.setId(id);
        interview.setTopicId(topicId);
        return interview;
    }
}
