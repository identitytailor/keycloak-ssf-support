package com.identitytailor.keycloak.ssf.transmitter.storage.memory;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Storage for SSF events.
 */
@JBossLog
public class InMemoryEventStore implements SsfEventStore {

    private static final Map<String, SecurityEventToken> events = new ConcurrentHashMap<>();
    private static final Queue<String> eventQueue = new ConcurrentLinkedQueue<>();
    
    private final KeycloakSession session;

    public InMemoryEventStore(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Stores an event for later polling.
     *
     * @param event The event to store
     */
    public void storeEvent(SecurityEventToken event) {
        String eventId = event.getJti();
        events.put(eventId, event);
        eventQueue.add(eventId);
    }

    /**
     * Gets events for polling.
     *
     * @param maxEvents The maximum number of events to return
     * @return A list of events
     */
    public List<SecurityEventToken> getEvents(int maxEvents) {
        List<SecurityEventToken> result = new ArrayList<>();
        int count = 0;
        
        for (String eventId : eventQueue) {
            if (count >= maxEvents) {
                break;
            }
            
            SecurityEventToken event = events.get(eventId);
            if (event != null) {
                result.add(event);
                count++;
            }
        }
        
        return result;
    }

    /**
     * Acknowledges an event.
     *
     * @param eventId The ID of the event to acknowledge
     */
    public void acknowledgeEvent(String eventId) {
        events.remove(eventId);
        eventQueue.remove(eventId);
    }

    /**
     * Checks if there are more events available.
     *
     * @return true if there are more events available, false otherwise
     */
    public boolean hasMoreEvents() {
        return !eventQueue.isEmpty();
    }
}
