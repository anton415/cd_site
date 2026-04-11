package ru.job4j.site.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import ru.job4j.site.exception.RemoteResourceNotFoundException;
import ru.job4j.site.exception.RemoteServiceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private final String url;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        var status = response.getStatusCode();
        var responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        var message = responseBody.isBlank()
                ? String.format("Remote call to %s failed with status %d %s",
                url, status.value(), status.getReasonPhrase())
                : String.format("Remote call to %s failed with status %d %s. Response: %s",
                url, status.value(), status.getReasonPhrase(), responseBody);
        if (status == HttpStatus.NOT_FOUND) {
            throw new RemoteResourceNotFoundException(message, url);
        }
        throw new RemoteServiceException(message, url, status.value());
    }
}
