package ru.job4j.site.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.job4j.site.dto.ErrorMessage;
import ru.job4j.site.exception.RemoteResourceNotFoundException;
import ru.job4j.site.exception.RemoteServiceException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class RemoteExceptionHandler {

    @ExceptionHandler(RemoteResourceNotFoundException.class)
    public Object handleRemoteResourceNotFound(RemoteResourceNotFoundException exception,
                                               HttpServletRequest request) {
        log.warn("Remote resource not found for request {}: {}", request.getRequestURI(), exception.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(exception.getMessage()));
        }
        return "redirect:/";
    }

    @ExceptionHandler(RemoteServiceException.class)
    public Object handleRemoteServiceException(RemoteServiceException exception,
                                               HttpServletRequest request) {
        log.error("Remote service call failed for request {}: {}",
                request.getRequestURI(), exception.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorMessage(exception.getMessage()));
        }
        return "redirect:/";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        var accept = request.getHeader(HttpHeaders.ACCEPT);
        return request.getRequestURI().contains("_rest")
                || request.getRequestURI().startsWith("/filter")
                || accept != null && accept.contains("application/json");
    }
}
