package com.identitytailor.keycloak.ssf.event.types.scim;

public abstract class ScimProvisioningEvent extends ScimEvent {

    public ScimProvisioningEvent(String type) {
        super(type);
    }
}
