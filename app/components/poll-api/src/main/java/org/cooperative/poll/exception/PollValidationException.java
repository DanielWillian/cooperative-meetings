package org.cooperative.poll.exception;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
public class PollValidationException extends RuntimeException {
    List<Validation> validations;

    @Builder
    public PollValidationException(String message, Throwable throwable,
            @Singular List<Validation> validations) {
        super(message, throwable);
        this.validations = validations;
    }
}
