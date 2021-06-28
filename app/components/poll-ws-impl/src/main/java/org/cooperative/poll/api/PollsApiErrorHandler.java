package org.cooperative.poll.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice("org.cooperative.poll.api")
public class PollsApiErrorHandler {

    @ExceptionHandler(PollsApiException.class)
    ResponseEntity<?> handlePollsApiException(PollsApiException ex, WebRequest request) {
        log.info("Handling exception: {} for request: {}", ex, request);
        return ResponseEntity
                .status(ex.getStatus())
                .build();
    }
}
