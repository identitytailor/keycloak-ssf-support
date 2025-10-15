package com.identitytailor.keycloak.ssf.transmitter.streams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a stream status in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamStatus {

    @JsonProperty("stream_id")
    private String streamId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;
}
