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

    @JsonProperty("returnImmediately")
    protected Boolean returnImmediately;

    @JsonProperty("maxEvents")
    protected Integer maxEvents;

    @JsonProperty("ack")
    protected List<String> ack;

    @JsonProperty("setErrs")
    protected List<String> setErrs;

}
