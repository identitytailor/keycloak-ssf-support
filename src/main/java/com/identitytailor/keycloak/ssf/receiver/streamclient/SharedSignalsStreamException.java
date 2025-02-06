package com.identitytailor.keycloak.ssf.receiver.streamclient;

import com.identitytailor.keycloak.ssf.SharedSignalsException;

public class SharedSignalsStreamException extends SharedSignalsException {

    public SharedSignalsStreamException() {
    }

    public SharedSignalsStreamException(String message) {
        super(message);
    }

    public SharedSignalsStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
