package com.identitytailor.keycloak.ssf.event.types.caep;

import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;

public abstract class CaepEvent extends SecurityEvent {

    public CaepEvent(String type) {
        super(type);
    }
}
