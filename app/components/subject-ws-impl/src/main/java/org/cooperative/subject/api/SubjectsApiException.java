package org.cooperative.subject.api;

import lombok.Builder;
import lombok.Value;
import org.cooperative.subject.api.model.Error;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Value
public class SubjectsApiException extends RuntimeException {
    HttpStatus status;
    Error error;

    public SubjectsApiException(String message, Throwable throwable) {
        super(message, throwable);
        this.status = null;
        this.error = null;
    }

    @Builder
    public SubjectsApiException(String message, Throwable throwable, HttpStatus status, Error error) {
        super(message, throwable);
        this.status = status;
        this.error = error;
    }

    public Optional<Error> getError() {
        return Optional.ofNullable(error);
    }
}
