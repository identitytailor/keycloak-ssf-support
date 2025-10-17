package com.identitytailor.keycloak.ssf.transmitter.streams;

import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterService;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing SSF streams.
 */
@JBossLog
public class StreamService {

    private final KeycloakSession session;
    private final SsfStreamStore streamStore;
    private final TransmitterService transmitterService;

    public StreamService(KeycloakSession session, SsfStreamStore streamStore, TransmitterService transmitterService) {
        this.session = session;
        this.streamStore = streamStore;
        this.transmitterService = transmitterService;
    }

    private boolean isAudience(StreamConfiguration stream) {
        if (stream == null) {
            return false;
        }
        String receiverClientId = session.getContext().getAuthenticationSession().getClient().getClientId();
        if (stream.getAudience() == null || !stream.getAudience().contains(receiverClientId)) {
            log.warnf("Authorization failed. Client '%s' attempted to access stream '%s' owned by '%s'.",
                    receiverClientId, stream.getStreamId(), stream.getAudience());
            return false;
        }
        return true;
    }

    /**
     * Creates a new stream.
     *
     * @param streamConfiguration The stream configuration
     * @return The created stream configuration
     */
    public StreamConfiguration createStream(StreamConfiguration streamConfiguration) {
        // Generate a new stream ID if not provided
        if (streamConfiguration.getStreamId() == null) {
            streamConfiguration.setStreamId(UUID.randomUUID().toString());
        }
        
        // Set default status if not provided
        if (streamConfiguration.getStatus() == null) {
            streamConfiguration.setStatus("enabled");
        }

        // set issuer
        String iss = transmitterService.getTransmitterMetadata().getIssuer();
        streamConfiguration.setIssuer(iss);

        // set current client as audience
        String receiverClientId = session.getContext().getAuthenticationSession().getClient().getClientId();
        streamConfiguration.setAudience(Set.of(receiverClientId));

        // set events delivered
        Set<String> eventsRequested = streamConfiguration.getEventsRequested();
        Set<String> eventsDelivered = new HashSet<>(eventsRequested);
        eventsDelivered.retainAll(transmitterService.getSupportedEvents());
        streamConfiguration.setEventsDelivered(eventsDelivered);

        // set events supported
        Set<String> eventsSupported = transmitterService.getSupportedEvents();
        streamConfiguration.setEventsSupported(eventsSupported);
        
        // Set timestamps
        int now = Time.currentTime();
        streamConfiguration.setCreatedAt(now);
        streamConfiguration.setUpdatedAt(now);
        
        // Store the stream configuration
        streamStore.saveStream(streamConfiguration);
        
        return streamConfiguration;
    }

    /**
     * Gets a stream by ID.
     *
     * @param streamId The stream ID
     * @return The stream configuration, or null if not found
     */
    public StreamConfiguration getStream(String streamId) {
        StreamConfiguration stream = streamStore.getStream(streamId);
        if (isAudience(stream)) {
            return stream;
        }
        return null;
    }

    /**
     * Gets all streams.
     *
     * @return A list of all stream configurations
     */
    public List<StreamConfiguration> getAllStreams() {
        return streamStore.getAllStreams().stream()
            .filter(stream -> isAudience(stream))
            .collect(Collectors.toList());
    }

    /**
     * Updates a stream.
     *
     * @param streamConfiguration The updated stream configuration
     * @return The updated stream configuration, or null if not found
     */
    public StreamConfiguration updateStream(StreamConfiguration streamConfiguration) {
        String streamId = streamConfiguration.getStreamId();
        StreamConfiguration existingStream = getStream(streamId);
        
        if (existingStream == null) {
            return null;
        }
        
        // Update timestamp
        existingStream.setUpdatedAt(Time.currentTime());
        existingStream.setDescription(streamConfiguration.getDescription());

        // set events requested
        Set<String> eventsRequested = streamConfiguration.getEventsRequested();
        existingStream.setEventsRequested(eventsRequested);

        // set events delivered
        Set<String> eventsDelivered = new HashSet<>(eventsRequested);
        eventsDelivered.retainAll(transmitterService.getSupportedEvents());
        existingStream.setEventsDelivered(eventsDelivered);

        
        // Store the updated stream configuration
        streamStore.saveStream(existingStream);
        
        return existingStream;
    }


    public StreamConfiguration replaceStream(StreamConfiguration streamConfiguration) {

        String streamId = streamConfiguration.getStreamId();
        StreamConfiguration existingStream = getStream(streamId);

        if (existingStream == null) {
            return null;
        }

        // Update timestamp
        existingStream.setUpdatedAt(Time.currentTime());
        existingStream.setDescription(streamConfiguration.getDescription());

        // set events requested
        Set<String> eventsRequested = streamConfiguration.getEventsRequested();
        existingStream.setEventsRequested(eventsRequested);

        // set events delivered
        Set<String> eventsDelivered = new HashSet<>(eventsRequested);
        eventsDelivered.retainAll(transmitterService.getSupportedEvents());
        existingStream.setEventsDelivered(eventsDelivered);


        // Store the updated stream configuration
        streamStore.saveStream(existingStream);

        return existingStream;
    }

    /**
     * Deletes a stream.
     *
     * @param streamId The stream ID
     * @return true if the stream was deleted, false if not found
     */
    public boolean deleteStream(String streamId) {
        StreamConfiguration existingStream = getStream(streamId);
        
        if (existingStream == null) {
            return false;
        }
        
        streamStore.deleteStream(streamId);
        return true;
    }

    /**
     * Gets the status of a stream.
     *
     * @param streamId The stream ID
     * @return The stream status, or null if not found
     */
    public StreamStatus getStreamStatus(String streamId) {
        StreamConfiguration stream = getStream(streamId);

        if (stream == null) {
            return null;
        }

        StreamStatus status = new StreamStatus();
        status.setStreamId(streamId);
        status.setStatus(stream.getStatus());
        status.setReason(stream.getStatusReason());

        return status;
    }

    /**
     * Updates the status of a stream.
     *
     * @param newStreamStatus The updated stream status
     * @return The updated stream status, or null if not found
     */
    public StreamStatus updateStreamStatus(StreamStatus newStreamStatus) {

        if (newStreamStatus == null) {
            return null;
        }

        String streamId = newStreamStatus.getStreamId();
        if (streamId == null) {
            return null;
        }

        StreamConfiguration stream = getStream(streamId);
        if (stream == null) {
            return null;
        }

        StreamStatus currentStreamStatus = streamStore.getStreamStatus(streamId);
        if (Objects.equals(currentStreamStatus.getStatus(), newStreamStatus.getStatus())) {
            // return current stream status
            return currentStreamStatus;
        }

        // TODO check if new status is allowed

        // Update the stream status
        streamStore.updateStreamStatus(streamId, newStreamStatus);
        stream.setUpdatedAt(Time.currentTime());

        // Update stream status
        return streamStore.updateStreamStatus(streamId, newStreamStatus);
    }
}
