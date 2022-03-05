package org.cooperative.poll.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.subject.SubjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice("org.cooperative.poll.api")
public class PollsApiErrorHandler {

    @ExceptionHandler(PollValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Mono<ErrorApi> handlePollValidationException(PollValidationException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(PollNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Mono<ErrorApi> handlePollNotFoundException(PollNotFoundException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(SubjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Mono<ErrorApi> handleSubjectNotFoundException(SubjectNotFoundException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }
}
