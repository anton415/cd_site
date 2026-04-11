package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.job4j.site.domain.Category;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.TopicLiteDTO;
import ru.job4j.site.util.RestAuthCall;

import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CategoriesService {

    private final TopicsService topicsService;
    private final InterviewsService interviewsService;
    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "desc";
    private static final String DIRECT_SINGLE = "/category/";
    private static final String DIRECT_MULTIPLE = "/categories/";

    public List<CategoryDTO> getAll() throws JsonProcessingException {
        var text = new RestAuthCall(String
                .format("%s%s", uriProvider.getUri(SERVICE_ID), DIRECT_MULTIPLE))
                .get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    public List<CategoryDTO> getPopularFromDesc() throws JsonProcessingException {
        var text = new RestAuthCall(String
                .format("%s%smost_pop", uriProvider.getUri(SERVICE_ID), DIRECT_MULTIPLE))
                .get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    public CategoryDTO create(String token, CategoryDTO category) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var out = new RestAuthCall(String
                .format("%s%s", uriProvider.getUri(SERVICE_ID), DIRECT_SINGLE))
                .post(
                token,
                mapper.writeValueAsString(category)
        );
        return mapper.readValue(out, CategoryDTO.class);
    }

    public void update(String token, CategoryDTO category) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        new RestAuthCall(String.format("%s%s", uriProvider.getUri(SERVICE_ID), DIRECT_SINGLE))
                .put(
                token,
                mapper.writeValueAsString(category)
        );
    }

    public List<CategoryDTO> getAllWithTopics() throws JsonProcessingException {
        return setInterviewCount(getAll());
    }

    public List<CategoryDTO> getMostPopular() throws JsonProcessingException {
        return setInterviewCount(getPopularFromDesc());
    }

    public String getNameById(List<CategoryDTO> list, int id) {
        String result = "";
        for (CategoryDTO category : list) {
            if (id == category.getId()) {
                result = category.getName();
                break;
            }
        }
        return result;
    }

    /**
     * Метод устанавливает количество собеседований для каждой категории.
     * @param categoriesDTO List<CategoryDTO> - список категорий, для которых нужно установить количество собеседований.
     * @return List<CategoryDTO> - список категорий с установленным количеством собеседований.
     */
    private List<CategoryDTO> setInterviewCount(List<CategoryDTO> categoriesDTO) {
        Map<Integer, Long> countByCategoryId = getInterviewCountByCategoryId();
        for (var categoryDTO : categoriesDTO) {
            categoryDTO.setCountInterview(countByCategoryId.getOrDefault(categoryDTO.getId(), 0L));
        }
        return categoriesDTO;
    }

    /**
     * Метод получает количество собеседований для каждой категории.
     * @return Map<Integer, Long> - карта, где ключ - ID категории, а значение - количество собеседований для этой категории.
     */
    private Map<Integer, Long> getInterviewCountByCategoryId() {
        Map<Integer, Integer> topicIdToCategoryId = topicsService.getAllTopicLiteDTO().stream()
                .collect(Collectors.toMap(
                        TopicLiteDTO::getId,
                        TopicLiteDTO::getCategoryId,
                        (existing, ignored) -> existing
                ));
        return interviewsService.getNewInterviews().stream()
                .map(interview -> topicIdToCategoryId.get(interview.getTopicId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * Метод возвращает категорию по id
     *
     * @param categoryId int ID Category ID
     * @return Optional<Category>
     */
    public Optional<Category> getById(int categoryId) {
        Optional<Category> result = Optional.empty();
        try {
            var text = new RestAuthCall(String
                    .format("%s%s%d", uriProvider.getUri(SERVICE_ID), DIRECT_SINGLE, categoryId))
                    .get();
            var mapper = new ObjectMapper();
            result = Optional.of(mapper.readValue(text, new TypeReference<>() {
            }));
        } catch (Exception e) {
            log.error("API category service not found, error:{}", e.getMessage());
        }
        return result;
    }
}
