package com.identitytailor.keycloak.ssf.transmitter.streams;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.NoCache;

import java.util.List;

/**
 * Endpoint for managing SSF streams.
 */
@JBossLog
public class StreamManagementEndpoint {

    private final StreamService streamService;

    public StreamManagementEndpoint(StreamService streamService) {
        this.streamService = streamService;
    }

    /**
     * Creates a new stream.
     *
     * @param streamConfiguration The stream configuration
     * @return The created stream configuration
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createStream(StreamConfiguration streamConfiguration) {
        try {
            StreamConfiguration createdStream = streamService.createStream(streamConfiguration);
            return Response.status(Response.Status.CREATED).entity(createdStream).build();
        } catch (Exception e) {
            log.error("Error creating stream", e);
            return Response.serverError().build();
        }
    }

    /**
     * Gets a stream by ID.
     *
     * @param streamId The stream ID
     * @return The stream configuration
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStream(@QueryParam("stream_id") String streamId) {
        try {
            if (streamId != null) {
                return getStreamById(streamId);
            }

            return getStreams();
        } catch (Exception e) {
            log.error("Error getting stream", e);
            return Response.serverError().build();
        }
    }

    protected Response getStreams() {
        List<StreamConfiguration> streams = streamService.getAllStreams();
        return Response.ok(streams).build();
    }

    protected Response getStreamById(String streamId) {
        StreamConfiguration stream = streamService.getStream(streamId);
        if (stream != null) {
            return Response.ok(stream).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Updates a stream.
     *
     * @param streamConfiguration The updated stream configuration
     * @return The updated stream configuration
     */
    @PATCH
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStream(StreamConfiguration streamConfiguration) {

        if (streamConfiguration == null || streamConfiguration.getStreamId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Stream ID is required").build();
        }

        try {
            StreamConfiguration updatedStream = streamService.updateStream(streamConfiguration);

            if (updatedStream == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(updatedStream).build();
        } catch (Exception e) {
            log.error("Error updating stream", e);
            return Response.serverError().build();
        }
    }

    /**
     * Replace a stream.
     *
     * @param streamConfiguration The updated stream configuration
     * @return The updated stream configuration
     */
    @PUT
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response replaceStream(StreamConfiguration streamConfiguration) {

        if (streamConfiguration == null || streamConfiguration.getStreamId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Stream ID is required").build();
        }

        try {
            StreamConfiguration replacedStream = streamService.replaceStream(streamConfiguration);

            if (replacedStream == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(replacedStream).build();
        } catch (Exception e) {
            log.error("Error replacing stream", e);
            return Response.serverError().build();
        }
    }

    /**
     * Deletes a stream.
     *
     * @param streamId The stream ID
     * @return A response indicating success or failure
     */
    @DELETE
    @NoCache
    public Response deleteStream(@QueryParam("stream_id") String streamId) {

        if (streamId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Stream ID is required").build();
        }

        try {
            boolean deleted = streamService.deleteStream(streamId);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting stream", e);
            return Response.serverError().build();
        }
    }
}
