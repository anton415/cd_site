package ru.job4j.site.exception;

import lombok.Getter;

@Getter
public class RemoteServiceException extends RuntimeException {
    private final String url;
    private final int statusCode;

    public RemoteServiceException(String message, String url, int statusCode) {
        super(message);
        this.url = url;
        this.statusCode = statusCode;
    }

    public RemoteServiceException(String message, String url, int statusCode, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.statusCode = statusCode;
    }
}
