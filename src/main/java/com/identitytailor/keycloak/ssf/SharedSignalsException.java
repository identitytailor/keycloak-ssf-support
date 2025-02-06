package com.identitytailor.keycloak.ssf;

public class SharedSignalsException extends RuntimeException {

    public SharedSignalsException() {
    }

    public SharedSignalsException(String message) {
        super(message);
    }

    public SharedSignalsException(String message, Throwable cause) {
        super(message, cause);
    }
}
