package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;
import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.ErrorSecurityEventToken;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.parser.SharedSignalsParsingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SecurityEventPollingResponseDeserializer extends JsonDeserializer<Map<String, SecurityEventToken>> {

    @Override
    public Map<String, SecurityEventToken> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        SharedSignalsProvider provider = SharedSignalsProvider.current();

        Map<String, SecurityEventToken> setsMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String jti = entry.getKey();  // Extract the SET ID (jti)
            String jwtString = entry.getValue().asText(); // Extract JWT string

            SecurityEventToken eventToken;
            try {
                eventToken = provider.parseSecurityEventToken(jwtString, /*processingContext*/ null);
            } catch (SharedSignalsParsingException parsingException) {
                eventToken = new ErrorSecurityEventToken(SharedSignalsFailureResponse.ERROR_AUTHENTICATION_FAILED, parsingException.getMessage());
            }

            eventToken.setId(jti); // Set jti manually since it's part of the key
            setsMap.put(jti, eventToken);
        }

        return setsMap;
    }

}