package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.SharedSignalsException;

public class SharedSignalsParsingException extends SharedSignalsException {

    public SharedSignalsParsingException(String message) {
        super(message);
    }

    public SharedSignalsParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
