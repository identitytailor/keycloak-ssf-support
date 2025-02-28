package com.identitytailor.keycloak.ssf.storage;

import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.verification.VerificationState;
import org.keycloak.models.RealmModel;

public interface SharedSignalsStore {

    void setVerificationState(RealmModel realm, ReceiverModel model, String state);

    VerificationState getVerificationState(RealmModel realm, ReceiverModel model);

    void clearVerificationState(RealmModel realm, ReceiverModel model);
}
