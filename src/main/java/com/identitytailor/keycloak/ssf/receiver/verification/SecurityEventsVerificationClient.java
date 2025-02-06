package com.identitytailor.keycloak.ssf.receiver.verification;

import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;

/**
 * See: https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-7.1.4
 */
public interface SecurityEventsVerificationClient {

    void requestVerification(ReceiverModel receiverModel, SharedSignalsTransmitterMetadata transmitterMetadata, String state);
}
