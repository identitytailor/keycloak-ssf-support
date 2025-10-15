package com.identitytailor.keycloak.ssf.transmitter.event;

import com.identitytailor.keycloak.ssf.event.types.VerificationEvent;
import com.identitytailor.keycloak.ssf.event.types.caep.CredentialChange;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionRevoked;
import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterService;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Time;
import org.keycloak.events.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator for Security Event Tokens (SETs).
 */
@JBossLog
public class SecurityEventTokenMapper {

    private final TransmitterService transmitterService;

    public SecurityEventTokenMapper(TransmitterService transmitterService) {
        this.transmitterService = transmitterService;
    }

    /**
     * Generates a verification event for a stream.
     *
     * @param stream The stream configuration
     * @param state  The verification state
     * @return The verification event as a JSON string
     */
    public SecurityEventToken generateVerificationEvent(StreamConfiguration stream, String state) {
        try {
            SecurityEventToken verificationEventToken = newSecurityEventToken();

            // Set audience to the stream's audience
            if (stream.getDelivery() != null && stream.getDelivery().getEndpointUrl() != null) {
                verificationEventToken.setAud(stream.getAudience().toArray(String[]::new));
            }

            // Set transaction ID
            verificationEventToken.setTxn(UUID.randomUUID().toString());

            // Set subject ID
            Map<String, Object> subId = new HashMap<>();
            subId.put("format", "opaque");
            subId.put("id", stream.getStreamId());
            verificationEventToken.setSubId(subId);

            // Set events
            Map<String, Object> events = new HashMap<>();
            Map<String, Object> verificationEvent = new HashMap<>();
            verificationEvent.put("state", state);
            events.put(VerificationEvent.TYPE, verificationEvent);
            verificationEventToken.setEvents(events);

            return verificationEventToken;
        } catch (Exception e) {
            log.error("Error generating verification event", e);
            return null;
        }
    }

    private SecurityEventToken newSecurityEventToken() {
        SecurityEventToken verificationEventToken = new SecurityEventToken();

        verificationEventToken.setJti(UUID.randomUUID().toString());
        verificationEventToken.setIss(transmitterService.getTransmitterMetadata().getIssuer());
        verificationEventToken.setIat(Time.currentTime());
        return verificationEventToken;
    }


    /**
     * Generates a session revoked event.
     *
     * @param sessionId The ID of the revoked session
     * @param userId    The ID of the user
     * @param reason    The reason for the revocation
     * @return The session revoked event as a SecurityEventToken
     */
    public SecurityEventToken generateSessionRevokedEvent(String sessionId, String userId, String reason) {
        try {
            SecurityEventToken eventToken = newSecurityEventToken();
            eventToken.setTxn(UUID.randomUUID().toString());

            // Set subject ID (complex subject with user and session)
            Map<String, Object> subId = new HashMap<>();
            subId.put("format", "complex");

            Map<String, Object> userSubject = new HashMap<>();
            userSubject.put("format", "iss_sub");
            userSubject.put("iss", transmitterService.getTransmitterMetadata().getIssuer());
            userSubject.put("sub", userId);

            Map<String, Object> sessionSubject = new HashMap<>();
            sessionSubject.put("format", "opaque");
            sessionSubject.put("id", sessionId);

            subId.put("user", userSubject);
            subId.put("session", sessionSubject);
            eventToken.setSubId(subId);

            // Set events
            Map<String, Object> events = new HashMap<>();
            Map<String, Object> sessionRevokedEvent = new HashMap<>();

            if (reason != null) {
                Map<String, Object> reasonAdmin = new HashMap<>();
                reasonAdmin.put("en", reason);
                sessionRevokedEvent.put("reason_admin", reasonAdmin);
            }

            sessionRevokedEvent.put("event_timestamp", Time.currentTime());
            events.put(SessionRevoked.TYPE, sessionRevokedEvent);
            eventToken.setEvents(events);

            return eventToken;
        } catch (Exception e) {
            log.error("Error generating session revoked event", e);
            return null;
        }
    }

    /**
     * Generates a credential change event.
     *
     * @param userId         The ID of the user
     * @param credentialType The type of credential that changed
     * @return The credential change event as a SecurityEventToken
     */
    public SecurityEventToken generateCredentialChangeEvent(String userId, String credentialType) {
        try {
            SecurityEventToken event = newSecurityEventToken();
            event.setTxn(UUID.randomUUID().toString());

            // Set subject ID
            Map<String, Object> subId = new HashMap<>();
            subId.put("format", "iss_sub");
            subId.put("iss", transmitterService.getTransmitterMetadata().getIssuer());
            subId.put("sub", userId);
            event.setSubId(subId);

            // Set events
            Map<String, Object> events = new HashMap<>();
            Map<String, Object> credentialChangeEvent = new HashMap<>();

            credentialChangeEvent.put("credential_type", credentialType);
            credentialChangeEvent.put("event_timestamp", Time.currentTime());
            events.put(CredentialChange.TYPE, credentialChangeEvent);
            event.setEvents(events);

            return event;
        } catch (Exception e) {
            log.error("Error generating credential change event", e);
            return null;
        }
    }

    public boolean isSupportedEvent(Event event) {
        return switch (event.getType()) {
            case LOGOUT, UPDATE_CREDENTIAL, UPDATE_PASSWORD -> true;
            default -> false;
        };
    }

    public SecurityEventToken toSecurityEvent(Event event) {

        if (!isSupportedEvent(event)) {
            return null;
        }

        SecurityEventToken securityEvent = switch (event.getType()) {
            case LOGOUT -> generateSessionRevokedEvent(
                    event.getSessionId(),
                    event.getUserId(),
                    "User logout"
            );
            case UPDATE_CREDENTIAL -> generateCredentialChangeEvent(
                    event.getUserId(),
                    event.getDetails().get("credential_type")
            );
            // Add more event mappings as needed

            default ->
                // Ignore other events
                    null;
        };
        // Map Keycloak events to SSF events

        return securityEvent;
    }
}
