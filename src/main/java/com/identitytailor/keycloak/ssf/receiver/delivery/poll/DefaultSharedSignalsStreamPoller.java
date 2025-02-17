package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.ErrorSecurityEventToken;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.verification.SharedSignalsStreamVerificationException;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * RFC 8936 Poll-Based Security Event Token (SET) Delivery Using HTTP
 * <p>
 * https://www.rfc-editor.org/rfc/rfc8936.html
 */
@JBossLog
public class DefaultSharedSignalsStreamPoller implements SharedSignalsStreamPoller {

    protected final KeycloakSession session;

    public DefaultSharedSignalsStreamPoller(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void pollEvents(SecurityEventPollingContext pollingContext, RealmModel realm, ReceiverModel receiverModel) {

        SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);

        SecurityEventPollingRequest pollingRequest = createPollingRequest(pollingContext, receiverModel);

        log.tracef("Sending polling request. %s", pollingRequest);
        SimpleHttp pollingHttp = preparePollingHttpClient(pollingRequest, receiverModel);

        try (var response = pollingHttp.asResponse()) {
            log.tracef("Received polling response. status=%s", response.getStatus());
            if (response.getStatus() == 200) {
                pollingContext.markSecurityEventAsAcknowledged(pollingRequest.getAck());
            }
            SecurityEventPollingResponse pollingResponse = response.asJson(SecurityEventPollingResponse.class);
            if (pollingResponse != null) {
                processPollingResponse(realm, pollingContext, pollingRequest, pollingResponse, sharedSignals, receiverModel);
            }
        } catch (IOException e) {
            log.warnf(e, "Failed to fetch events from polling endpoint. %s", pollingRequest);
        }

        if (!receiverModel.isAcknowledgeImmediately()) {
            return;
        }

        sendAcknowledgements(pollingContext, pollingRequest, receiverModel);
    }

    private void sendAcknowledgements(SecurityEventPollingContext pollingContext, SecurityEventPollingRequest pollingRequest, ReceiverModel receiverModel) {

        if (!pollingContext.hasSecurityEventsToAcknowledge()) {
            return;
        }

        log.tracef("Sending ack-only request. %s", pollingRequest);
        SecurityEventPollingRequest ackOnlyRequest = createAckOnlyRequest(pollingContext.getSecurityEventIdsToAcknowledge());
        SimpleHttp pollingHttp = preparePollingHttpClient(ackOnlyRequest, receiverModel);

        try (var response = pollingHttp.asResponse()) {
            log.tracef("Received acknowledge response. status=%s", response.getStatus());
            SecurityEventPollingResponse pollingResponse = response.asJson(SecurityEventPollingResponse.class);
            if (pollingResponse != null) {
                processAcknowledgementResponse(pollingContext, pollingResponse, pollingRequest);
            }
        } catch (IOException e) {
            log.warnf(e, "Failed to acknowledge events from polling endpoint. %s", pollingRequest);
        }
    }

    protected void processAcknowledgementResponse(SecurityEventPollingContext pollingContext, SecurityEventPollingResponse pollingResponse, SecurityEventPollingRequest pollingRequest) {
        pollingContext.markSecurityEventAsAcknowledged();
    }

    protected SecurityEventPollingRequest createPollingRequest(SecurityEventPollingContext pollingContext, ReceiverModel receiverModel) {
        if (pollingContext.hasSecurityEventsToAcknowledge()) {
            return createPollAndAckRequest(pollingContext, receiverModel);
        } else {
            return createPollOnlyRequest(receiverModel);
        }
    }

    protected SimpleHttp preparePollingHttpClient(SecurityEventPollingRequest pollingRequest, ReceiverModel receiverModel) {
        String transmitterPollUrl = receiverModel.getTransmitterPollUrl();
        String transmitterAccessToken = receiverModel.getTransmitterAccessToken();
        return SimpleHttp.doPost(transmitterPollUrl, session) //
                .auth(transmitterAccessToken) //
                .connectTimeoutMillis(receiverModel.getConnectTimeout()) //
                .socketTimeOutMillis(receiverModel.getSocketTimeout()) //
                .json(pollingRequest);
    }

    protected void processPollingResponse(RealmModel realm, SecurityEventPollingContext pollingContext, SecurityEventPollingRequest pollingRequest, SecurityEventPollingResponse
            pollingResponse, SharedSignalsProvider sharedSignals, ReceiverModel receiverModel) {

        Map<String, String> sets = pollingResponse.getSets();
        if (sets == null || sets.isEmpty()) {
            log.tracef("No SETs found.");
            return;
        }

        SecurityEventProcessingContext processingContext = sharedSignals.createSecurityEventProcessingContext(null, receiverModel.getAlias());

        log.tracef("%s SETs found.", sets.size());
        for (var setEntry : sets.entrySet()) {
            String jti = setEntry.getKey();
            String encodedSet = setEntry.getValue();
            SecurityEventToken securityEventToken = sharedSignals.parseSecurityEventToken(encodedSet, processingContext);
            securityEventToken.setId(jti);

            processingContext.setSecurityEventToken(securityEventToken);
            processSecurityEventToken(pollingContext, sharedSignals, securityEventToken, processingContext);
        }

        if (CollectionUtil.isNotEmpty(pollingRequest.getAck())) {
            pollingContext.markSecurityEventAsAcknowledged();
        }
    }

    protected void processSecurityEventToken(SecurityEventPollingContext pollingContext, SharedSignalsProvider
            provider, SecurityEventToken securityEventToken, SecurityEventProcessingContext processingContext) {

        try {
            provider.processSecurityEvents(processingContext);
        } catch (SharedSignalsStreamVerificationException sssve) {
            log.warnf(sssve, "Failed to verify stream. %s", securityEventToken);
            // use errorCode invalid_state, see: https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-7.1.4.1
            var errorSecurityEventToken = new ErrorSecurityEventToken("invalid_state", sssve.getMessage());
            errorSecurityEventToken.setId(securityEventToken.getId());
            securityEventToken = errorSecurityEventToken;
        } catch (Exception e) {
            log.warnf(e, "Failed to process security event token. %s", securityEventToken);
            var errorSecurityEventToken = new ErrorSecurityEventToken("processing_error", e.getMessage());
            errorSecurityEventToken.setId(securityEventToken.getId());
            securityEventToken = errorSecurityEventToken;
        }

        if (securityEventToken instanceof ErrorSecurityEventToken errorSecurityEventToken) {
            pollingContext.registerSecurityEventForError(securityEventToken.getId(), errorSecurityEventToken.getFailureResponse());
            return;
        }

        if (processingContext.isProcessedSuccessfully()) {
            // mark token JTI for acknowledgement
            pollingContext.registerSecurityEventForAcknowledgment(securityEventToken.getId());
        }
    }

    protected SecurityEventPollingRequest createPollAndAckRequest(SecurityEventPollingContext pollingContext, ReceiverModel receiverModel) {

        Set<String> acks = pollingContext.getSecurityEventIdsToAcknowledge();

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.POLL_AND_ACK);
        request.setReturnImmediately(true);
        request.setMaxEvents(receiverModel.getMaxEvents());
        request.setAck(acks);

        return request;
    }

    protected SecurityEventPollingRequest createAckOnlyRequest(Set<String> acknowledgedSecurityEventIds) {

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.ACK_ONLY);
        request.setReturnImmediately(true);
        request.setMaxEvents(0);
        request.setAck(acknowledgedSecurityEventIds);

        return request;
    }

    protected SecurityEventPollingRequest createPollOnlyRequest(ReceiverModel receiverModel) {

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.POLL_ONLY);
        request.setReturnImmediately(true);
        request.setMaxEvents(receiverModel.getMaxEvents());

        return request;
    }

}
