package com.identitytailor.keycloak.ssf.event.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.identitytailor.keycloak.ssf.event.SecurityEvents;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SecurityEventMapJsonDeserializer extends JsonDeserializer<Map<String, SecurityEvent>> {

    @Override
    public Map<String, SecurityEvent> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        Map<String, SecurityEvent> eventsMap = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String eventType = entry.getKey();  // Extracts event type key
            JsonNode eventData = entry.getValue(); // Extracts event data

            Class<? extends SecurityEvent> eventClass = SecurityEvents.getSecurityEventType(eventType);

            if (eventClass == null) {
                throw new IOException("Unknown event type: " + eventType);
            }

            SecurityEvent event = mapper.treeToValue(eventData, eventClass);
            event.eventType = eventType;  // Manually set event type since it's not in JSON
            eventsMap.put(eventType, event);
        }

        return eventsMap;
    }
}
