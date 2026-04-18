package ru.job4j.site.exception;

import org.springframework.http.HttpStatus;

public class RemoteResourceNotFoundException extends RemoteServiceException {

    public RemoteResourceNotFoundException(String message, String url) {
        super(message, url, HttpStatus.NOT_FOUND.value());
    }
}
