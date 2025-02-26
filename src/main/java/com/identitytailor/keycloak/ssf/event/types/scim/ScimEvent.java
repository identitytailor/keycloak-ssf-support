package com.identitytailor.keycloak.ssf.event.types.scim;

import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;

public abstract class ScimEvent extends SecurityEvent {

    public ScimEvent(String type) {
        super(type);
    }
}
