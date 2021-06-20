package org.cooperative.subject;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException() {
    }

    public SubjectNotFoundException(String message) {
        super(message);
    }

    public SubjectNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SubjectNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
