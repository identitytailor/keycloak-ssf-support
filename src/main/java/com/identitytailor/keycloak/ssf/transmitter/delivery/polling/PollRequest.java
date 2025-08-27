package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents a poll request in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollRequest {

    @JsonProperty("return_immediately")
    private Boolean returnImmediately;

    @JsonProperty("max_events")
    private Integer maxEvents;

    @JsonProperty("ack")
    private List<String> ack;
}
