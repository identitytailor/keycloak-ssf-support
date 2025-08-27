package com.identitytailor.keycloak.ssf.transmitter.storage.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JPA implementation of EventStore that stores events in a relational database.
 */
@JBossLog
public class JpaEventStore implements SsfEventStore {

    private final KeycloakSession session;
    private final EntityManager em;
    private final ObjectMapper objectMapper;
    private static final AtomicLong sequenceCounter = new AtomicLong(0);

    public JpaEventStore(KeycloakSession session) {
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.objectMapper = new ObjectMapper();
        
        // Initialize sequence counter if needed
        initializeSequenceCounter();
    }
    
    private void initializeSequenceCounter() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT MAX(e.sequenceOrder) FROM SsfEventEntity e", Long.class);
            Long maxSequence = query.getSingleResult();
            
            if (maxSequence != null) {
                sequenceCounter.set(maxSequence + 1);
            }
        } catch (Exception e) {
            log.warn("Could not initialize sequence counter, using default value", e);
        }
    }

    @Override
    public void storeEvent(SecurityEventToken event) {
        try {
            SsfEventEntity entity = new SsfEventEntity();
            entity.setId(event.getJti());
            entity.setStreamId(null); // Set if needed based on your business logic
            entity.setEventData(objectMapper.writeValueAsString(event));
            entity.setCreatedAt(System.currentTimeMillis() / 1000);
            entity.setAcknowledged(false);
            entity.setSequenceOrder(sequenceCounter.getAndIncrement());
            
            em.persist(entity);
            em.flush();
        } catch (JsonProcessingException e) {
            log.error("Error storing event", e);
            throw new RuntimeException("Error storing event", e);
        }
    }

    @Override
    public List<SecurityEventToken> getEvents(int maxEvents) {
        try {
            TypedQuery<SsfEventEntity> query = em.createQuery(
                "SELECT e FROM SsfEventEntity e WHERE e.acknowledged = false ORDER BY e.sequenceOrder ASC",
                SsfEventEntity.class);
            query.setMaxResults(maxEvents);
            
            List<SsfEventEntity> entities = query.getResultList();
            List<SecurityEventToken> events = new ArrayList<>();
            
            for (SsfEventEntity entity : entities) {
                SecurityEventToken event = objectMapper.readValue(entity.getEventData(), SecurityEventToken.class);
                events.add(event);
            }
            
            return events;
        } catch (Exception e) {
            log.error("Error getting events", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void acknowledgeEvent(String eventId) {
        try {
            SsfEventEntity entity = em.find(SsfEventEntity.class, eventId);
            
            if (entity != null) {
                entity.setAcknowledged(true);
                em.merge(entity);
                em.flush();
            }
        } catch (Exception e) {
            log.error("Error acknowledging event", e);
            throw new RuntimeException("Error acknowledging event", e);
        }
    }

    @Override
    public boolean hasMoreEvents() {
        try {
            Query query = em.createQuery("SELECT COUNT(e) FROM SsfEventEntity e WHERE e.acknowledged = false");
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking for more events", e);
            return false;
        }
    }
}
