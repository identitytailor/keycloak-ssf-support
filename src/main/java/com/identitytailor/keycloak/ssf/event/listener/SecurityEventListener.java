package com.identitytailor.keycloak.ssf.event.listener;

import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;

public interface SecurityEventListener {

    void onSecurityEvent(SecurityEventProcessingContext processingContext, String securityEventId, SecurityEvent securityEvent);

}
