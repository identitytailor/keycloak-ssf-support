package com.identitytailor.keycloak.ssf.event.types.risc;

import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;

public abstract class RiscEvent extends SecurityEvent {

    public RiscEvent(String type) {
        super(type);
    }
}
