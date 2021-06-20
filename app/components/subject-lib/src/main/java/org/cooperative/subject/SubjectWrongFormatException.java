package org.cooperative.subject;

public class SubjectWrongFormatException extends RuntimeException {
    public SubjectWrongFormatException() {
    }

    public SubjectWrongFormatException(String message) {
        super(message);
    }

    public SubjectWrongFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SubjectWrongFormatException(Throwable throwable) {
        super(throwable);
    }
}
