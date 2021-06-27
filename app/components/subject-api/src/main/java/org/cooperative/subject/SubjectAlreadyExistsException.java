package org.cooperative.subject;

public class SubjectAlreadyExistsException extends RuntimeException {
    public SubjectAlreadyExistsException() {
    }

    public SubjectAlreadyExistsException(String message) {
        super(message);
    }

    public SubjectAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SubjectAlreadyExistsException(Throwable throwable) {
        super(throwable);
    }
}
