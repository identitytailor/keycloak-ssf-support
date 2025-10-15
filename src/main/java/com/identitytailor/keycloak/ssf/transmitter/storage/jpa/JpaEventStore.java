package com.identitytailor.keycloak.ssf.transmitter.storage.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA implementation of EventStore that stores events in a relational database.
 */
@JBossLog
public class JpaEventStore implements SsfEventStore {

    private final KeycloakSession session;
    private final EntityManager em;
    private final ObjectMapper objectMapper;

    public JpaEventStore(KeycloakSession session) {
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void storeEvent(String streamId, SecurityEventToken event) {
        try {
            SsfEventEntity entity = new SsfEventEntity();
            entity.setId(event.getJti());
            entity.setStreamId(streamId);
            entity.setEventData(objectMapper.writeValueAsString(event));
            entity.setCreatedAt(System.currentTimeMillis() / 1000);
            entity.setAcknowledged(false);

            em.persist(entity);
            em.flush();
        } catch (JsonProcessingException e) {
            log.error("Error storing event", e);
            throw new RuntimeException("Error storing event", e);
        }
    }

    @Override
    public List<SecurityEventToken> getEvents(String streamId, int maxEvents) {
        try {
            var query = em.createQuery("""
                            SELECT e 
                            FROM SsfEventEntity e 
                            WHERE e.streamId = :streamId 
                            and e.acknowledged = false 
                            and e.failed = false
                            ORDER BY e.createdAt ASC
                            """, SsfEventEntity.class) //
                    .setParameter("streamId", streamId);
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
    public void acknowledgeEvent(String streamId, String eventId) {
        try {
            SsfEventEntity entity = em.find(SsfEventEntity.class, eventId);
            if (entity != null && streamId.equals(entity.getStreamId())) {
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
    public void failedEvent(String streamId, String eventId) {
        try {
            SsfEventEntity entity = em.find(SsfEventEntity.class, eventId);
            if (entity != null && streamId.equals(entity.getStreamId())) {
                entity.setAcknowledged(false);
                entity.setFailed(true);
                em.merge(entity);
                em.flush();
            }
        } catch (Exception e) {
            log.error("Error acknowledging event", e);
            throw new RuntimeException("Error acknowledging event", e);
        }
    }

    @Override
    public boolean hasMoreEvents(String streamId) {
        try {
            Query query = em.createQuery("""
                    SELECT COUNT(e) 
                    FROM SsfEventEntity e 
                    WHERE e.streamId = :streamId AND e.acknowledged = false
                    """).setParameter("streamId", streamId);
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking for more events", e);
            return false;
        }
    }
}
