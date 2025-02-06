package com.identitytailor.keycloak.ssf.event.subjects;

import com.identitytailor.keycloak.ssf.SharedSignalsException;

public class SubjectParsingException extends SharedSignalsException {

    public SubjectParsingException() {
    }

    public SubjectParsingException(String message) {
        super(message);
    }

    public SubjectParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
