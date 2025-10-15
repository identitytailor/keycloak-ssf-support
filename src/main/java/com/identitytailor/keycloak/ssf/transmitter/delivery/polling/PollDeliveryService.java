package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling event delivery using the POLL delivery method.
 */
@JBossLog
public class PollDeliveryService {

    private final KeycloakSession session;
    private final SecurityEventTokenEncoder securityEventTokenEncoder;
    private final SsfEventStore eventStore;

    public PollDeliveryService(KeycloakSession session, SecurityEventTokenEncoder securityEventTokenEncoder, SsfEventStore eventStore) {
        this.session = session;
        this.securityEventTokenEncoder = securityEventTokenEncoder;
        this.eventStore = eventStore;
    }

    /**
     * Polls for events.
     *
     * @param streamId
     * @param maxEvents         The maximum number of events to return
     * @param returnImmediately Whether to return immediately or wait for events
     * @return The poll response containing events
     */
    public PollResponse pollEvents(String streamId, int maxEvents, boolean returnImmediately) {
        try {
            List<SecurityEventToken> events = eventStore.getEvents(streamId, maxEvents);

            if (events.isEmpty() && !returnImmediately) {
                // wait
                TimeUnit.SECONDS.sleep(1); // TODO make this configurable
                // then check for new elements to arrive
                events = eventStore.getEvents(streamId, maxEvents);
            }

            Map<String, String> setMap = new HashMap<>(events.size());
            for (SecurityEventToken secEvent : events) {
                String jti = secEvent.getJti();
                String encodedEvent = securityEventTokenEncoder.encode(secEvent);
                setMap.put(jti, encodedEvent);
            }
            
            PollResponse response = new PollResponse();
            response.setSets(setMap);
            response.setMoreAvailable(eventStore.hasMoreEvents(streamId));
            
            return response;
        } catch (RuntimeException e) {
            log.error("Error polling events", e);
            throw e;
        } catch (InterruptedException e) {
            log.error("Error polling events", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Acknowledges received events.
     *
     * @param streamId
     * @param eventIds The IDs of the events to acknowledge
     * @return true if the events were acknowledged successfully, false otherwise
     */
    public boolean handleEventAcks(String streamId, Set<String> eventIds) {
        try {
            for (String eventId : eventIds) {
                eventStore.acknowledgeEvent(streamId, eventId);
                log.debugf("Acknowledged event for streamId '%s' with id '%s'", streamId, eventId);
            }
            return true;
        } catch (Exception e) {
            log.error("Error acknowledging events", e);
            return false;
        }
    }

    /**
     * Stores an event for later polling.
     *
     * @param streamId
     * @param event  The event to store
     * @return true if the event was stored successfully, false otherwise
     */
    public boolean storeEvent(String streamId, SecurityEventToken event) {
        try {
            eventStore.storeEvent(streamId, event);
            log.debugf("Stored event for streamId '%s' with id '%s'", streamId, event.getJti());
            return true;
        } catch (Exception e) {
            log.error("Error storing event", e);
            return false;
        }
    }

    public boolean handleEventErrors(String streamId, Set<String> setErrs) {
        try {
            for (String eventId : setErrs) {
                eventStore.failedEvent(streamId, eventId);
                log.debugf("Failed event for streamId '%s' with id '%s'", streamId, eventId);
            }
            return true;
        } catch (Exception e) {
            log.error("Error acknowledging events", e);
            return false;
        }
    }
}
