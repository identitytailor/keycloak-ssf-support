package com.identitytailor.keycloak.ssf.transmitter.delivery.polling;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;

/**
 * Endpoint for polling events from the SSF transmitter.
 */
@JBossLog
public class PollEndpoint {

    private final KeycloakSession session;

    private final PollDeliveryService pollDeliveryService;

    public PollEndpoint(KeycloakSession session, PollDeliveryService pollDeliveryService) {
        this.session = session;
        this.pollDeliveryService = pollDeliveryService;
    }

    /**
     * Polls for events or acknowledges received events.
     *
     * @param pollRequest The poll request
     * @return The poll response containing events or acknowledgment status
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollEvents(PollRequest pollRequest) {
        try {
            // Check if this is an acknowledgment request
            if (pollRequest.getAck() != null && !pollRequest.getAck().isEmpty()) {
                boolean success = pollDeliveryService.acknowledgeEvents(pollRequest.getAck());
                return Response.ok().build();
            }
            
            // This is a poll request
            Integer maxEvents = pollRequest.getMaxEvents();
            Boolean returnImmediately = pollRequest.getReturnImmediately();
            
            if (maxEvents == null) {
                maxEvents = 100; // Default value
            }
            
            if (returnImmediately == null) {
                returnImmediately = true; // Default value
            }
            
            PollResponse response = pollDeliveryService.pollEvents(maxEvents, returnImmediately);
            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("Error polling events", e);
            return Response.serverError().build();
        }
    }
}
