package com.identitytailor.keycloak.ssf.transmitter.storage;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;

import java.util.List;

/**
 * Interface for storing and retrieving SSF events.
 */
public interface SsfEventStore {

    /**
     * Stores an event for later polling.
     *
     * @param event The event to store
     */
    void storeEvent(SecurityEventToken event);

    /**
     * Gets events for polling.
     *
     * @param maxEvents The maximum number of events to return
     * @return A list of events
     */
    List<SecurityEventToken> getEvents(int maxEvents);

    /**
     * Acknowledges an event.
     *
     * @param eventId The ID of the event to acknowledge
     */
    void acknowledgeEvent(String eventId);

    /**
     * Checks if there are more events available.
     *
     * @return true if there are more events available, false otherwise
     */
    boolean hasMoreEvents();
}
