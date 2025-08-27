package com.identitytailor.keycloak.ssf.transmitter.verification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a verification request in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationRequest {

    @JsonProperty("stream_id")
    private String streamId;

    @JsonProperty("state")
    private String state;
}
