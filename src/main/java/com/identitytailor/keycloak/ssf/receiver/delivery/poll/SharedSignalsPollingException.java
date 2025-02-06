package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsException;

public class SharedSignalsPollingException extends SharedSignalsException {

    public SharedSignalsPollingException() {
    }

    public SharedSignalsPollingException(String message) {
        super(message);
    }

    public SharedSignalsPollingException(String message, Throwable cause) {
        super(message, cause);
    }
}
