package com.identitytailor.keycloak.ssf.event.processor;

public interface SecurityEventProcessor {

    void processSecurityEvents(SecurityEventProcessingContext context);
}
