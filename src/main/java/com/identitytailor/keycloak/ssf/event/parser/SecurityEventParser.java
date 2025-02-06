package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;

public interface SecurityEventParser {

    SecurityEventToken parse(String encodedSecurityEventToken);
}
