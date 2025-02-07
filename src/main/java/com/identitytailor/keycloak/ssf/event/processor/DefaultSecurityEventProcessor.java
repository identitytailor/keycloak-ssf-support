package com.identitytailor.keycloak.ssf.event.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.SecurityEvents;
import com.identitytailor.keycloak.ssf.event.listener.SecurityEventListener;
import com.identitytailor.keycloak.ssf.event.parser.SharedSignalsParsingException;
import com.identitytailor.keycloak.ssf.event.subjects.OpaqueSubjectId;
import com.identitytailor.keycloak.ssf.event.subjects.SubjectId;
import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;
import com.identitytailor.keycloak.ssf.event.types.VerificationEvent;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.verification.SharedSignalsStreamVerificationException;
import com.identitytailor.keycloak.ssf.receiver.verification.VerificationState;
import com.identitytailor.keycloak.ssf.storage.SharedSignalsStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Map;

@JBossLog
public class DefaultSecurityEventProcessor implements SecurityEventProcessor {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final SecurityEventListener securityEventListener;

    protected final SharedSignalsStore sharedSignalsStore;

    public DefaultSecurityEventProcessor(SharedSignalsProvider sharedSignals, SecurityEventListener securityEventListener, SharedSignalsStore sharedSignalsStore) {
        this.securityEventListener = securityEventListener;
        this.sharedSignalsStore = sharedSignalsStore;
    }

    @Override
    public void processSecurityEvents(SecurityEventProcessingContext processingContext) {

        SecurityEventToken securityEventToken = processingContext.getSecurityEventToken();
        Map<String, Map<String, Object>> events = securityEventToken.getEvents();
        for (var entry : events.entrySet()) {
            String jti = securityEventToken.getId();
            String securityEventType = entry.getKey();
            Map<String, Object> securityEventData = entry.getValue();

            try {
                SecurityEvent securityEvent = convertEventDataToSecurityEvent(securityEventType, securityEventData);

                if (securityEvent instanceof VerificationEvent verificationEvent) {
                    // handle verification event

                    if (events.size() > 1) {
                        log.warnf("Found more than one security event for token with verification request. %s", jti);
                    }

                    if (handleVerificationEvent(processingContext, verificationEvent, jti)) {
                        break;
                    }
                } else {
                    // handle verification event
                    handleEvent(processingContext, jti, securityEvent);
                }
            } catch (final SharedSignalsParsingException sspe) {
                throw sspe;
            }
        }

        processingContext.setProcessedSuccessfully(true);
    }

    protected SecurityEvent convertEventDataToSecurityEvent(String securityEventType, Map<String, Object> securityEventData) {

        Class<? extends SecurityEvent> eventClass = SecurityEvents.getSecurityEventType(securityEventType);

        if (eventClass == null) {
            throw new SharedSignalsParsingException("Could not parse security event. Unknown event type: " + securityEventType);
        }

        try {
            SecurityEvent securityEvent = OBJECT_MAPPER.convertValue(securityEventData, eventClass);
            securityEvent.setEventType(securityEventType);
            return securityEvent;
        } catch (Exception e) {
            throw new SharedSignalsParsingException("Could not parse security event.", e);
        }
    }


    protected boolean handleVerificationEvent(SecurityEventProcessingContext processingContext, VerificationEvent verificationEvent, String jti) {

        KeycloakContext keycloakContext = processingContext.getSession().getContext();

        String streamId = extractStreamIdFromVerificationEvent(processingContext, verificationEvent);

        RealmModel realm = keycloakContext.getRealm();
        ReceiverModel receiverModel = processingContext.getReceiver().getReceiverModel();

        if (!receiverModel.getStreamId().equals(streamId)) {
            log.debugf("Verification failed! StreamId missmatch. jti=%s expectedStreamId=%s actualStreamId=%s", jti, receiverModel.getStreamId(), streamId);
            return false;
        }

        VerificationState verificationState = getVerificationState(realm, receiverModel);

        String givenState = verificationEvent.getState();
        String expectedState = verificationState == null ? null : verificationState.getState();

        if (givenState.equals(expectedState)) {
            log.debugf("Verification successful!. %s", jti);
            sharedSignalsStore.clearVerificationState(realm, receiverModel);
            return true;
        }

        log.warnf("Verification failed. %s", jti);
        throw new SharedSignalsStreamVerificationException("Verification state mismatch.");
    }

    protected VerificationState getVerificationState(RealmModel realm, ReceiverModel receiverModel) {
        return sharedSignalsStore.getVerificationState(realm, receiverModel);
    }

    protected String extractStreamIdFromVerificationEvent(SecurityEventProcessingContext processingContext, SecurityEvent securityEvent) {
        // see: https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-7.1.4.2

        String streamId = null;

        // See: https://openid.net/specs/openid-sharedsignals-framework-1_0.html#section-7.1.4.1
        // try to extract subjectId from securityEvent
        SubjectId subjectId = securityEvent.getSubjectId();
        if (subjectId instanceof OpaqueSubjectId opaqueSubjectId) {
            streamId = opaqueSubjectId.getId();
        }

        if (streamId == null) {
            // as a fallback, try to extract subjectId from securityEventToken
            subjectId = processingContext.getSecurityEventToken().getSubjectId();
            if (subjectId instanceof OpaqueSubjectId opaqueSubjectId) {
                streamId = opaqueSubjectId.getId();
            }
        }

        // TODO find a reliable way to extract the streamId from the verification event
        if (streamId == null) {
            throw new SharedSignalsStreamVerificationException("Could not find stream id for verification request");
        }
        return streamId;
    }

    protected void handleEvent(SecurityEventProcessingContext processingContext, String jti, SecurityEvent securityEvent) {
        securityEventListener.onSecurityEvent(processingContext, jti, securityEvent);
    }
}
