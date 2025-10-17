package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Represents a poll response in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollResponse {

    @JsonProperty("sets")
    protected Map<String, String> sets;

    @JsonProperty("moreAvailable")
    protected boolean moreAvailable;
}
