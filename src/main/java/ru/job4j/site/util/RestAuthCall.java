package ru.job4j.site.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.job4j.site.exception.RemoteServiceException;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class RestAuthCall {
    private final String url;

    public String get() {
        return execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), new ParameterizedTypeReference<String>() {
                    }
            ).getBody();
        });
    }

    public String get(String token) {
        return execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + token);
            return restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), new ParameterizedTypeReference<String>() {
                    }
            ).getBody();
        });
    }

    public String getWithHeaders(HttpHeaders headers) {
        return execute(() -> {
            var restTemplate = restTemplate();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), new ParameterizedTypeReference<String>() {
                    }
            ).getBody();
        });
    }

    public String token(Map<String, String> params) {
        return execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic am9iNGo6cGFzc3dvcmQ=");
            var map = new LinkedMultiValueMap<String, String>();
            params.forEach(map::add);
            map.add("scope", "any");
            map.add("grant_type", "password");
            return restTemplate.postForEntity(
                    url, new HttpEntity<>(map, headers), String.class
            ).getBody();
        });
    }

    public String post(String token, String json) {
        return execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            return restTemplate.postForEntity(
                    url, new HttpEntity<>(json, headers), String.class
            ).getBody();
        });
    }

    public void update(String token, String json) {
        execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            var request = new HttpEntity<>(json, headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            return null;
        });
    }

    public void delete(String token, String json) {
        execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            var request = new HttpEntity<>(json, headers);
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            return null;
        });
    }

    public void put(String token, String json) {
        execute(() -> {
            var restTemplate = restTemplate();
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            restTemplate.put(
                    url, new HttpEntity<>(json, headers), String.class
            );
            return null;
        });
    }

    private RestTemplate restTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler(url));
        return restTemplate;
    }

    private <T> T execute(RestOperation<T> operation) {
        try {
            return operation.execute();
        } catch (RemoteServiceException e) {
            throw e;
        } catch (RestClientException e) {
            throw new RemoteServiceException(
                    String.format("Remote call to %s failed: %s", url, e.getMessage()),
                    url,
                    -1,
                    e
            );
        }
    }

    @FunctionalInterface
    private interface RestOperation<T> {
        T execute();
    }
}
