package com.identitytailor.keycloak.ssf.receiver.verification;

import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

@JBossLog
public class DefaultSecurityEventsVerificationClient implements SecurityEventsVerificationClient {

    private final KeycloakSession session;

    public DefaultSecurityEventsVerificationClient(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void requestVerification(ReceiverModel model, SharedSignalsTransmitterMetadata metadata, String state) {

        var verificationRequest = new VerificationRequest();
        verificationRequest.setStreamId(model.getStreamId());
        verificationRequest.setState(state);

        log.debugf("Sending verification request to %s. %s", metadata.getVerificationEndpoint(), verificationRequest);
        SimpleHttp verificationHttpCall = prepareHttpCall(metadata.getVerificationEndpoint(), model.getTransmitterAccessToken(), verificationRequest);
        try (var response = verificationHttpCall.asResponse()) {
            log.debugf("Received verification response. status=%s", response.getStatus());

            if (response.getStatus() != 204) {
                throw new SharedSignalsStreamVerificationException("Expected a 204 response but got: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new SharedSignalsStreamVerificationException("Could not send verification request", e);
        }
    }

    protected SimpleHttp prepareHttpCall(String verifyUri, String token, VerificationRequest verificationRequest) {
        return SimpleHttp.doPost(verifyUri, session).auth(token).json(verificationRequest);
    }
}
