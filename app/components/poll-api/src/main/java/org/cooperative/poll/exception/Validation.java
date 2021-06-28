package org.cooperative.poll.exception;

public enum Validation {
    NAME_TOO_LONG, MISSING_START_DATE,
    END_DATE_EARLIER_THAN_START_DATE, MISSING_SUBJECT_ID,
    MISSING_ID
}
