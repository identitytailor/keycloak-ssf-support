package com.identitytailor.keycloak.ssf.event;

import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;

public class ErrorSecurityEventToken extends SecurityEventToken {

    protected SharedSignalsFailureResponse failureResponse;

    public ErrorSecurityEventToken(String errorCode, String message) {
        this.failureResponse = new SharedSignalsFailureResponse(errorCode, message);
    }

    public SharedSignalsFailureResponse getFailureResponse() {
        return failureResponse;
    }

    @Override
    public String toString() {
        return "ErrorSecurityEventToken{" +
               "failureResponse=" + failureResponse +
               '}';
    }
}
