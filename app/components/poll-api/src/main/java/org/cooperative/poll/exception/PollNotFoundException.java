package org.cooperative.poll.exception;

public class PollNotFoundException extends RuntimeException {
    public PollNotFoundException() {
    }

    public PollNotFoundException(String message) {
        super(message);
    }

    public PollNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public PollNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
