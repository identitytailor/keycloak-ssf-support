package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import lombok.Data;

import java.util.List;

/**
 * Represents a poll response in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollResponse {

    @JsonProperty("more")
    private Boolean more;

    @JsonProperty("sets")
    private List<SecurityEventToken> sets;

}
