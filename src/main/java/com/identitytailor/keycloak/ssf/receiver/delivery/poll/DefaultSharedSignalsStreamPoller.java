package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.ErrorSecurityEventToken;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsHacks;
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
    public void pollEvents(SecurityEventPollingContext pollingContext, SecurityEventPollingConfig config, RealmModel realm, ReceiverModel receiverModel) {

        SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);

        SecurityEventPollingPolicy pollingPolicy = config.getPollingPolicy();

        SecurityEventPollingRequest pollingRequest = createPollingRequest(pollingContext, pollingPolicy);

        log.tracef("Sending polling request. %s", pollingRequest);
        SimpleHttp pollingHttp = preparePollingHttpClient(pollingRequest, receiverModel);

        session.setAttribute(SharedSignalsHacks.RECEIVER_MODEL_SESSION_ATTRIBUTE, receiverModel);
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
        } finally {
            session.removeAttribute(SharedSignalsHacks.RECEIVER_MODEL_SESSION_ATTRIBUTE);
        }

        if (!config.isAcknowledgeImmediately()) {
            return;
        }

        sendAcknowledgements(pollingContext, pollingRequest, pollingPolicy, receiverModel);
    }

    private void sendAcknowledgements(SecurityEventPollingContext pollingContext, SecurityEventPollingRequest pollingRequest, SecurityEventPollingPolicy pollingPolicy, ReceiverModel receiverModel) {

        if (!pollingContext.hasSecurityEventsToAcknowledge()) {
            return;
        }

        log.tracef("Sending ack-only request. %s", pollingRequest);
        SecurityEventPollingRequest ackOnlyRequest = createAckOnlyRequest(pollingPolicy, pollingContext.getSecurityEventIdsToAcknowledge());
        SimpleHttp pollingHttp = preparePollingHttpClient(ackOnlyRequest, receiverModel);

        session.setAttribute(SharedSignalsHacks.RECEIVER_MODEL_SESSION_ATTRIBUTE, receiverModel);
        try (var response = pollingHttp.asResponse()) {
            log.tracef("Received acknowledge response. status=%s", response.getStatus());
            SecurityEventPollingResponse pollingResponse = response.asJson(SecurityEventPollingResponse.class);
            if (pollingResponse != null) {
                processAcknowledgementResponse(pollingContext, pollingResponse, pollingRequest);
            }
        } catch (IOException e) {
            log.warnf(e, "Failed to acknowledge events from polling endpoint. %s", pollingRequest);
        } finally {
            session.removeAttribute(SharedSignalsHacks.RECEIVER_MODEL_SESSION_ATTRIBUTE);
        }
    }

    protected void processAcknowledgementResponse(SecurityEventPollingContext pollingContext, SecurityEventPollingResponse pollingResponse, SecurityEventPollingRequest pollingRequest) {
        pollingContext.markSecurityEventAsAcknowledged();
    }

    protected SecurityEventPollingRequest createPollingRequest(SecurityEventPollingContext pollingContext, SecurityEventPollingPolicy pollingPolicy) {
        if (pollingContext.hasSecurityEventsToAcknowledge()) {
            return createPollAndAckRequest(pollingContext, pollingPolicy);
        } else {
            return createPollOnlyRequest(pollingPolicy);
        }
    }

    protected SimpleHttp preparePollingHttpClient(SecurityEventPollingRequest pollingRequest, ReceiverModel receiverModel) {
        String transmitterPollUrl = receiverModel.getTransmitterPollUrl();
        String transmitterAccessToken = receiverModel.getTransmitterAccessToken();
        return SimpleHttp.doPost(transmitterPollUrl, session).auth(transmitterAccessToken).json(pollingRequest);
    }

    protected void processPollingResponse(RealmModel realm, SecurityEventPollingContext pollingContext, SecurityEventPollingRequest pollingRequest, SecurityEventPollingResponse
            pollingResponse, SharedSignalsProvider sharedSignals, ReceiverModel receiverModel) {

        Map<String, SecurityEventToken> sets = pollingResponse.getSets();
        if (sets == null || sets.isEmpty()) {
            log.tracef("No SETs found.");
            return;
        }

        log.tracef("%s SETs found.", sets.size());
        for (var setEntry : sets.entrySet()) {
            SecurityEventToken securityEventToken = setEntry.getValue();
            processSecurityEventToken(pollingContext, sharedSignals, receiverModel, securityEventToken);
        }

        if (CollectionUtil.isNotEmpty(pollingRequest.getAck())) {
            pollingContext.markSecurityEventAsAcknowledged();
        }
    }

    protected void processSecurityEventToken(SecurityEventPollingContext pollingContext, SharedSignalsProvider
            provider, ReceiverModel receiverModel, SecurityEventToken securityEventToken) {

        SecurityEventProcessingContext processingContext = provider.createSecurityEventProcessingContext(securityEventToken, receiverModel.getAlias());
        try {
            provider.processSecurityEvents(processingContext);
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

    protected SecurityEventPollingRequest createPollAndAckRequest(SecurityEventPollingContext pollingContext, SecurityEventPollingPolicy pollingPolicy) {

        Set<String> acks = pollingContext.getSecurityEventIdsToAcknowledge();

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.POLL_AND_ACK);
        request.setReturnImmediately(true);
        request.setMaxEvents(pollingPolicy.getMaxEvents());
        request.setAck(acks);

        return request;
    }

    protected SecurityEventPollingRequest createAckOnlyRequest(SecurityEventPollingPolicy pollingPolicy, Set<String> acknowledgedSecurityEventIds) {

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.ACK_ONLY);
        request.setReturnImmediately(true);
        request.setMaxEvents(0);
        request.setAck(acknowledgedSecurityEventIds);

        return request;
    }

    protected SecurityEventPollingRequest createPollOnlyRequest(SecurityEventPollingPolicy pollingPolicy) {

        var request = new SecurityEventPollingRequest();
        request.setPollingMode(PollingMode.POLL_ONLY);
        request.setReturnImmediately(true);
        request.setMaxEvents(pollingPolicy.getMaxEvents());

        return request;
    }

}
