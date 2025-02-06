package com.identitytailor.keycloak.ssf.receiver.util;

import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class SharedSignalsResponseUtil {

    public static WebApplicationException newSharedSignalFailureResponse(Response.Status status, String errorCode, String errorMessage) {
        Response response = Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new SharedSignalsFailureResponse(errorCode, errorMessage))
                .build();
        return new WebApplicationException(response);
    }
}
