package org.cooperative.subject.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice("org.cooperative.subject.api")
public class SubjectsApiErrorHandler {

    @ExceptionHandler(SubjectsApiException.class)
    ResponseEntity<?> handleSubjectsApiException(SubjectsApiException ex, WebRequest request) {
        log.info("Handling exception: {} for request: {}", ex, request);
        return ResponseEntity
                .status(ex.getStatus())
                .body(ex.getError().orElse(null));
    }
}
