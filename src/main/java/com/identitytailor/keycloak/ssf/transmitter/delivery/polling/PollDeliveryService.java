package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

import java.util.List;

/**
 * Service for handling event delivery using the POLL delivery method.
 */
@JBossLog
public class PollDeliveryService {

    private final KeycloakSession session;
    private final SsfEventStore eventStore;

    public PollDeliveryService(KeycloakSession session, SsfEventStore eventStore) {
        this.session = session;
        this.eventStore = eventStore;
    }

    /**
     * Polls for events.
     *
     * @param maxEvents The maximum number of events to return
     * @param returnImmediately Whether to return immediately or wait for events
     * @return The poll response containing events
     */
    public PollResponse pollEvents(int maxEvents, boolean returnImmediately) {
        try {
            List<SecurityEventToken> events = eventStore.getEvents(maxEvents);
            
            PollResponse response = new PollResponse();
            response.setSets(events);
            response.setMore(eventStore.hasMoreEvents());
            
            return response;
        } catch (Exception e) {
            log.error("Error polling events", e);
            throw e;
        }
    }

    /**
     * Acknowledges received events.
     *
     * @param eventIds The IDs of the events to acknowledge
     * @return true if the events were acknowledged successfully, false otherwise
     */
    public boolean acknowledgeEvents(List<String> eventIds) {
        try {
            for (String eventId : eventIds) {
                eventStore.acknowledgeEvent(eventId);
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
     * @param event The event to store
     * @return true if the event was stored successfully, false otherwise
     */
    public boolean storeEvent(SecurityEventToken event) {
        try {
            eventStore.storeEvent(event);
            return true;
        } catch (Exception e) {
            log.error("Error storing event", e);
            return false;
        }
    }
}
