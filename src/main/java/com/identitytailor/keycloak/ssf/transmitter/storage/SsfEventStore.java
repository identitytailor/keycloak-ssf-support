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
     * @param streamId
     * @param event The event to store
     */
    void storeEvent(String streamId, SecurityEventToken event);

    /**
     * Gets events for polling.
     *
     * @param streamId
     * @param maxEvents The maximum number of events to return
     * @return A list of events
     */
    List<SecurityEventToken> getEvents(String streamId, int maxEvents);

    /**
     * Acknowledges an event.
     *
     * @param streamId
     * @param eventId  The ID of the event to acknowledge
     */
    void acknowledgeEvent(String streamId, String eventId);

    void failedEvent(String streamId, String eventId);

    /**
     * Checks if there are more events available.
     *
     * @param streamId
     * @return true if there are more events available, false otherwise
     */
    boolean hasMoreEvents(String streamId);
}
