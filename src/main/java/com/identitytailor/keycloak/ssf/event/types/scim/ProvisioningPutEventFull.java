package com.identitytailor.keycloak.ssf.event.types.scim;

public class ProvisioningPutEventFull extends ScimProvisioningEvent {

    /**
     * See: https://www.ietf.org/archive/id/draft-ietf-scim-events-07.html#section-2.4.3
     */
    public static final String TYPE = "urn:ietf:params:SCIM:event:prov:put:full";

    public ProvisioningPutEventFull() {
        super(TYPE);
    }
}
