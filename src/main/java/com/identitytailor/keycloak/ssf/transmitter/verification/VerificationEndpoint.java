package com.identitytailor.keycloak.ssf.transmitter.verification;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.NoCache;

/**
 * Endpoint for SSF stream verification.
 */
@JBossLog
public class VerificationEndpoint {

    private final VerificationService verificationService;

    public VerificationEndpoint(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /**
     * Triggers a verification event for a stream.
     *
     * @param verificationRequest The verification request
     * @return A response indicating success or failure
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerVerification(VerificationRequest verificationRequest) {
        try {
            if (verificationRequest.getStreamId() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Stream ID is required").build();
            }
            
            boolean success = verificationService.triggerVerification(verificationRequest);
            
            if (success) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Stream not found").build();
            }
        } catch (Exception e) {
            log.error("Error triggering verification", e);
            return Response.serverError().build();
        }
    }
}
