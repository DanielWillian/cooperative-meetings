package org.cooperative.subject.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.subject.SubjectAlreadyExistsException;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectWrongFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice("org.cooperative.subject.api")
public class SubjectsApiErrorHandler {

    private static final String EXCEPTION_LOG_FORMAT = "Handling exception: {}";
    private static final String WRONG_FORMAT_ID_EXCEPTION =
            "Id must be between 0 and " + Long.MAX_VALUE + "!";
    private static final String NOT_FOUND_MESSAGE = "Subject not found!";
    private static final String ALREADY_EXIST_MESSAGE = "Subject already exists!";

    @ExceptionHandler(SubjectWrongFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Mono<ErrorApi> handleSubjectWrongFormatException(SubjectWrongFormatException e) {
        log.info(EXCEPTION_LOG_FORMAT, e);
        return Mono.just(ErrorApi.of(WRONG_FORMAT_ID_EXCEPTION));
    }

    @ExceptionHandler(SubjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Mono<ErrorApi> handleSubjectNotFoundException(SubjectNotFoundException e) {
        log.info(EXCEPTION_LOG_FORMAT, e);
        return Mono.just(ErrorApi.of(NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(SubjectAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Mono<ErrorApi> handleSubjectAlreadyExistsException(SubjectAlreadyExistsException e) {
        log.info(EXCEPTION_LOG_FORMAT, e);
        return Mono.just(ErrorApi.of(ALREADY_EXIST_MESSAGE));
    }
}
