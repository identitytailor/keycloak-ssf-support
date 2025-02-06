package com.identitytailor.keycloak.ssf.receiver.streamclient;

import com.identitytailor.keycloak.ssf.SharedSignalsException;
import jakarta.ws.rs.core.Response;

public class SharedSignalsStreamException extends SharedSignalsException {

    private final Response.Status status;

    public SharedSignalsStreamException(Response.Status statusCode) {
        this.status = statusCode;
    }

    public SharedSignalsStreamException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public SharedSignalsStreamException(String message, Throwable cause, Response.Status status) {
        super(message, cause);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
