package org.cooperative.vote.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.vote.exception.PollAlreadyEndedException;
import org.cooperative.vote.exception.VoteAlreadyExistsException;
import org.cooperative.vote.exception.VoteNotFoundException;
import org.cooperative.vote.exception.VoteValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice("org.cooperative.vote.api")
public class VoteApiErrorHandler {
    @ExceptionHandler(VoteValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Mono<Void> handlePollValidationException(VoteValidationException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(VoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Mono<Void> handlePollNotFoundException(VoteNotFoundException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(PollNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Mono<Void> handleSubjectNotFoundException(PollNotFoundException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(VoteAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Mono<Void> handleSubjectNotFoundException(VoteAlreadyExistsException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }

    @ExceptionHandler(PollAlreadyEndedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    Mono<Void> handleSubjectNotFoundException(PollAlreadyEndedException e) {
        log.info("Handling exception: {}", e);
        return Mono.empty();
    }
}
