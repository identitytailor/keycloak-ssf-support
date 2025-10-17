package com.identitytailor.keycloak.ssf.transmitter.storage.memory;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Storage for SSF events.
 */
@JBossLog
public class InMemoryEventStore implements SsfEventStore {

    private static final Map<String, SecurityEventToken> events = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Queue<String>> eventQueues = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Set<String>> pendingAcks = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<String>> failedEvents = new ConcurrentHashMap<>();

    private final KeycloakSession session;

    public InMemoryEventStore(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Stores an event for later polling.
     *
     * @param event The event to store
     */
    public void storeEvent(String streamId, SecurityEventToken event) {
        String eventId = event.getJti();
        events.put(eventId, event);
        Set<String> pendingAcksForStream = getPendingAcks(pendingAcks, streamId);
        Queue<String> eventQueue = getEventQueue(streamId);
        if (eventQueue != null) {
            pendingAcksForStream.add(eventId);
            eventQueue.add(eventId);
        }
    }

    protected Queue<String> getEventQueue(String streamId) {
        return eventQueues.computeIfAbsent(streamId, sid -> new LinkedBlockingDeque<>());
    }

    /**
     * Gets events for polling.
     *
     * @param maxEvents The maximum number of events to return
     * @return A list of events
     */
    public List<SecurityEventToken> getEvents(String streamId, int maxEvents) {

        Queue<String> eventQueue = getEventQueue(streamId);
        if (eventQueue == null) {
            return List.of();
        }

        int count = 0;
        List<SecurityEventToken> result = new ArrayList<>();
        String eventId;
        do {
            eventId = eventQueue.poll();

            if (eventId != null) {
                SecurityEventToken event = events.get(eventId);
                result.add(event);
                events.remove(eventId); // consume the event
                count++;
            }
        } while (eventId != null && count < maxEvents);

        return result;
    }

    /**
     * Acknowledges an event.
     *
     * @param streamId
     * @param eventId  The ID of the event to acknowledge
     */
    public void acknowledgeEvent(String streamId, String eventId) {
        events.remove(eventId);
        Queue<String> eventQueue = getEventQueue(streamId);
        Set<String> pendingAcksForStream = getPendingAcks(pendingAcks, streamId);
        if (eventQueue != null) {
            pendingAcksForStream.remove(eventId);
            eventQueue.remove(eventId);
        }
    }

    @Override
    public void failedEvent(String streamId, String eventId) {
        events.remove(eventId);
        Queue<String> eventQueue = getEventQueue(streamId);
        Set<String> pendingAcksForStream = getPendingAcks(pendingAcks, streamId);
        Set<String> failedSets = getFailedSets(failedEvents, streamId);
        if (eventQueue != null) {
            eventQueue.remove(eventId);
            pendingAcksForStream.remove(eventId);
            failedSets.add(eventId);
        }
    }

    protected Set<String> getPendingAcks(ConcurrentMap<String, Set<String>> pendingAcks, String streamId) {
        return pendingAcks.computeIfAbsent(streamId, sid -> new ConcurrentSkipListSet<>());
    }

    protected Set<String> getFailedSets(ConcurrentMap<String, Set<String>> failedEvents, String streamId) {
        return failedEvents.computeIfAbsent(streamId, sid -> new ConcurrentSkipListSet<>());
    }

    /**
     * Checks if there are more events available.
     *
     * @return true if there are more events available, false otherwise
     */
    public boolean hasMoreEvents(String streamId) {
        Queue<String> eventQueue = getEventQueue(streamId);
        if (eventQueue != null) {
            return !eventQueue.isEmpty();
        }
        return false;
    }
}
