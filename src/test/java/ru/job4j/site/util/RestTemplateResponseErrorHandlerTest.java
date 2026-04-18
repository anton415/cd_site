package ru.job4j.site.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import ru.job4j.site.exception.RemoteResourceNotFoundException;
import ru.job4j.site.exception.RemoteServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestTemplateResponseErrorHandlerTest {

    @Test
    void whenHandleNotFoundThenThrowRemoteResourceNotFoundException() throws Exception {
        var handler = new RestTemplateResponseErrorHandler("http://service/topic/1");
        var response = new MockClientHttpResponse("not found".getBytes(), HttpStatus.NOT_FOUND);

        assertThat(handler.hasError(response)).isTrue();
        assertThatThrownBy(() -> handler.handleError(response))
                .isInstanceOf(RemoteResourceNotFoundException.class)
                .hasMessageContaining("http://service/topic/1")
                .hasMessageContaining("404");
    }

    @Test
    void whenHandleServerErrorThenThrowRemoteServiceException() throws Exception {
        var handler = new RestTemplateResponseErrorHandler("http://service/topic/1");
        var response = new MockClientHttpResponse("boom".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);

        assertThatThrownBy(() -> handler.handleError(response))
                .isInstanceOf(RemoteServiceException.class)
                .hasMessageContaining("http://service/topic/1")
                .hasMessageContaining("500")
                .hasMessageContaining("boom");
    }
}
