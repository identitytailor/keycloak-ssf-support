package com.identitytailor.keycloak.ssf.transmitter.storage.memory;

import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatus;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for SSF streams.
 */
@JBossLog
public class InMemoryStreamStore implements SsfStreamStore {

    private static final Map<String, StreamConfiguration> streams = new ConcurrentHashMap<>();

    public InMemoryStreamStore() {
    }

    /**
     * Saves a stream configuration.
     *
     * @param streamConfiguration The stream configuration to save
     */
    public void saveStream(StreamConfiguration streamConfiguration) {
        streams.put(streamConfiguration.getStreamId(), streamConfiguration);
    }

    @Override
    public StreamStatus updateStreamStatus(String streamId, StreamStatus streamStatus) {

        StreamConfiguration stream = getStream(streamId);
        if (stream == null) {
            return null;
        }

        stream.setUpdatedAt(Time.currentTime());
        stream.setStatus(streamStatus.getStatus());
        stream.setStatusReason(streamStatus.getReason());

        return streamStatus;
    }

    @Override
    public StreamStatus getStreamStatus(String streamId) {

        StreamConfiguration stream = getStream(streamId);
        if (stream == null) {
            return null;
        }

        StreamStatus streamStatus = new StreamStatus();
        streamStatus.setStreamId(streamId);
        streamStatus.setStatus(stream.getStatus());
        streamStatus.setReason(stream.getStatusReason());

        return streamStatus;
    }

    /**
     * Gets a stream configuration by ID.
     *
     * @param streamId The stream ID
     * @return The stream configuration, or null if not found
     */
    public StreamConfiguration getStream(String streamId) {
        return streams.get(streamId);
    }

    /**
     * Gets all stream configurations.
     *
     * @return A list of all stream configurations
     */
    public List<StreamConfiguration> getAllStreams() {
        return new ArrayList<>(streams.values());
    }

    /**
     * Deletes a stream configuration.
     *
     * @param streamId The stream ID
     */
    public void deleteStream(String streamId) {
        streams.remove(streamId);
    }
}
