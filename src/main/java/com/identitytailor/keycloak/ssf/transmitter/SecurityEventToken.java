package com.identitytailor.keycloak.ssf.transmitter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.keycloak.Token;
import org.keycloak.TokenCategory;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;

import java.util.Map;

/**
 * Represents a Security Event Token (SET) in the SSF transmitter.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityEventToken implements Token {

    @JsonProperty("jti")
    private String jti;

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("iat")
    private Integer iat;

    @JsonProperty("aud")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] aud;

    @JsonProperty("sub_id")
    private Map<String, Object> subId;

    @JsonProperty("events")
    private Map<String, Object> events;

    @JsonProperty("txn")
    private String txn;

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.ACCESS;
    }
}
