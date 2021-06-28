package org.cooperative.poll.api;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class PollsApiException extends RuntimeException {
    HttpStatus status;

    public PollsApiException(String message, Throwable throwable) {
        super(message, throwable);
        this.status = null;
    }

    @Builder
    public PollsApiException(String message, Throwable throwable, HttpStatus status) {
        super(message, throwable);
        this.status = status;
    }
}
