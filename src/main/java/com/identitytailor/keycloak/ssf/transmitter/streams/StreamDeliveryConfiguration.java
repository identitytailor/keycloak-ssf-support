package com.identitytailor.keycloak.ssf.transmitter.streams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Represents the delivery configuration for a stream.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamDeliveryConfiguration {

    @JsonProperty("method")
    private String method;

    @JsonProperty("endpoint_url")
    private String endpointUrl;

    @JsonProperty("authorization_header")
    private String authorizationHeader;

    @JsonProperty("additional_parameters")
    private Map<String, Object> additionalParameters;
}
