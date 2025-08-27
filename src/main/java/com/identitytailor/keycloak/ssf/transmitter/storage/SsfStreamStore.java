package com.identitytailor.keycloak.ssf.transmitter.storage;

import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatus;

import java.util.List;

/**
 * Interface for storing and retrieving SSF stream configurations.
 */
public interface SsfStreamStore {

    /**
     * Saves a stream configuration.
     *
     * @param streamConfiguration The stream configuration to save
     */
    void saveStream(StreamConfiguration streamConfiguration);

    /**
     * Update the stream status
     *
     * @param streamId
     * @param streamStatus
     * @return
     */
    StreamStatus updateStreamStatus(String streamId, StreamStatus streamStatus);

    /**
     * Get the stream status
     *
     * @param streamId
     * @return
     */
    StreamStatus getStreamStatus(String streamId);

    /**
     * Gets a stream configuration by ID.
     *
     * @param streamId The stream ID
     * @return The stream configuration, or null if not found
     */
    StreamConfiguration getStream(String streamId);

    /**
     * Gets all stream configurations.
     *
     * @return A list of all stream configurations
     */
    List<StreamConfiguration> getAllStreams();

    /**
     * Deletes a stream configuration.
     *
     * @param streamId The stream ID
     */
    void deleteStream(String streamId);
}
