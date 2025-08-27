package com.identitytailor.keycloak.ssf.transmitter.metadata;

import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;

/**
 * Endpoint that exposes the SSF transmitter configuration metadata.
 * This endpoint is accessible at /.well-known/ssf-configuration
 */
@JBossLog
public class TransmitterConfigurationEndpoint {

    private final KeycloakSession session;

    private final TransmitterService transmitterService;

    public TransmitterConfigurationEndpoint(KeycloakSession session, TransmitterService transmitterService) {
        this.session = session;
        this.transmitterService = transmitterService;
    }

    /**
     * Returns the SSF transmitter configuration metadata.
     * This is required by the SSF specification.
     *
     * @return The SSF transmitter configuration metadata
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransmitterMetadata() {
        try {
            SharedSignalsTransmitterMetadata metadata = transmitterService.getTransmitterMetadata();
            return Response.ok(metadata).build();
        } catch (Exception e) {
            log.error("Error retrieving transmitter metadata", e);
            return Response.serverError().build();
        }
    }
}
