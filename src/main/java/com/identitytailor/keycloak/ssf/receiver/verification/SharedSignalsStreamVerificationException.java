package com.identitytailor.keycloak.ssf.receiver.verification;

import com.identitytailor.keycloak.ssf.SharedSignalsException;

public class SharedSignalsStreamVerificationException extends SharedSignalsException {

    public SharedSignalsStreamVerificationException() {
    }

    public SharedSignalsStreamVerificationException(String message) {
        super(message);
    }

    public SharedSignalsStreamVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
