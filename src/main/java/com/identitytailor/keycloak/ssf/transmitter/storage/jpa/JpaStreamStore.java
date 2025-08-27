package com.identitytailor.keycloak.ssf.transmitter.storage.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamDeliveryConfiguration;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA implementation of StreamStore that stores stream configurations in a relational database.
 */
public class JpaStreamStore implements SsfStreamStore {

    private static final Logger logger = Logger.getLogger(JpaStreamStore.class);
    private final EntityManager em;

    public JpaStreamStore(KeycloakSession session) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @Override
    public void saveStream(StreamConfiguration streamConfiguration) {
        try {
            SsfStreamEntity entity = toEntity(streamConfiguration);
            
            // Check if the entity already exists
            SsfStreamEntity existing = em.find(SsfStreamEntity.class, entity.getId());
            
            if (existing != null) {
                // Update existing entity
                em.detach(existing);
                em.merge(entity);
            } else {
                // Create new entity
                em.persist(entity);
            }
            
            em.flush();
        } catch (Exception e) {
            logger.error("Error saving stream configuration", e);
            throw new RuntimeException("Error saving stream configuration", e);
        }
    }

    @Override
    public StreamStatus updateStreamStatus(String streamId, StreamStatus streamStatus) {

        SsfStreamEntity stream = em.find(SsfStreamEntity.class, streamId);
        if (stream == null) {
            return null;
        }

        stream.setStatus(streamStatus.getStatus());
        stream.setStatusReason(streamStatus.getReason());
        stream.setUpdatedAt(Time.currentTime());

        return streamStatus;
    }

    @Override
    public StreamStatus getStreamStatus(String streamId) {
        SsfStreamEntity entity = em.find(SsfStreamEntity.class, streamId);
        StreamStatus streamStatus = new StreamStatus();
        streamStatus.setStreamId(streamId);
        streamStatus.setStatus(entity.getStatus());
        streamStatus.setReason(entity.getStatusReason());
        return streamStatus;
    }

    @Override
    public StreamConfiguration getStream(String streamId) {
        try {
            SsfStreamEntity entity = em.find(SsfStreamEntity.class, streamId);
            
            if (entity == null) {
                return null;
            }
            
            return toModel(entity);
        } catch (Exception e) {
            logger.error("Error getting stream configuration", e);
            return null;
        }
    }

    @Override
    public List<StreamConfiguration> getAllStreams() {
        try {
            TypedQuery<SsfStreamEntity> query = em.createQuery("SELECT s FROM SsfStreamEntity s", SsfStreamEntity.class);
            List<SsfStreamEntity> entities = query.getResultList();
            
            return entities.stream()
                    .map(this::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting all stream configurations", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteStream(String streamId) {
        try {
            SsfStreamEntity entity = em.find(SsfStreamEntity.class, streamId);
            
            if (entity != null) {
                em.remove(entity);
                em.flush();
            }
        } catch (Exception e) {
            logger.error("Error deleting stream configuration", e);
            throw new RuntimeException("Error deleting stream configuration", e);
        }
    }

    /**
     * Converts a StreamConfiguration model to a StreamEntity.
     *
     * @param model The model to convert
     * @return The entity
     */
    private SsfStreamEntity toEntity(StreamConfiguration model) {
        try {
            SsfStreamEntity entity = new SsfStreamEntity();
            entity.setId(model.getStreamId());
            entity.setDescription(model.getDescription());
            entity.setStatus(model.getStatus());
            entity.setCreatedAt(model.getCreatedAt());
            entity.setUpdatedAt(model.getUpdatedAt());
            
            if (model.getDelivery() != null) {
                entity.setDeliveryMethod(model.getDelivery().getMethod());
                entity.setEndpointUrl(model.getDelivery().getEndpointUrl());
                entity.setAuthorizationHeader(model.getDelivery().getAuthorizationHeader());
                
                if (model.getDelivery().getAdditionalParameters() != null) {

                    entity.setAdditionalParameters(JsonSerialization.writeValueAsString(model.getDelivery().getAdditionalParameters()));
                }
            }
            
            if (model.getEventsRequested() != null) {
                entity.setEventsRequested(String.join(",", model.getEventsRequested()));
            }
            
            return entity;
        } catch (JsonProcessingException e) {
            logger.error("Error converting model to entity", e);
            throw new RuntimeException("Error converting model to entity", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a StreamEntity to a StreamConfiguration model.
     *
     * @param entity The entity to convert
     * @return The model
     */
    private StreamConfiguration toModel(SsfStreamEntity entity) {
        try {
            StreamConfiguration model = new StreamConfiguration();
            model.setStreamId(entity.getId());
            model.setDescription(entity.getDescription());
            model.setStatus(entity.getStatus());
            model.setStatusReason(entity.getStatusReason());
            model.setCreatedAt(entity.getCreatedAt());
            model.setUpdatedAt(entity.getUpdatedAt());
            
            StreamDeliveryConfiguration delivery = new StreamDeliveryConfiguration();
            delivery.setMethod(entity.getDeliveryMethod());
            delivery.setEndpointUrl(entity.getEndpointUrl());
            delivery.setAuthorizationHeader(entity.getAuthorizationHeader());
            
            if (entity.getAdditionalParameters() != null) {
                delivery.setAdditionalParameters(JsonSerialization.readValue(entity.getAdditionalParameters(), Map.class));
            }
            
            model.setDelivery(delivery);
            
            if (entity.getEventsRequested() != null) {
                model.setEventsRequested(Set.of(entity.getEventsRequested().split(",")));
            }
            
            return model;
        } catch (JsonProcessingException e) {
            logger.error("Error converting entity to model", e);
            throw new RuntimeException("Error converting entity to model", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
