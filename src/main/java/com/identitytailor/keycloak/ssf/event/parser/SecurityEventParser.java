package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;

public interface SecurityEventParser {

    SecurityEventToken parseSecurityEventToken(String encodedSecurityEventToken, SharedSignalsReceiver receiver);
}
