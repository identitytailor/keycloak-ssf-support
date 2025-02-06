package com.identitytailor.keycloak.ssf.storage;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import org.keycloak.models.RealmModel;

public interface SharedSignalsEventStore {

    void storeSecurityEvent(RealmModel realm, ReceiverModel receiverModel, SecurityEventToken securityEvent);
}
