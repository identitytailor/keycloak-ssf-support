package com.identitytailor.keycloak.ssf.transmitter.delivery.push;

import com.identitytailor.keycloak.ssf.Ssf;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.entity.StringEntity;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

/**
 * Service for delivering events using the PUSH delivery method.
 */
@JBossLog
public class PushDeliveryService {

    private final KeycloakSession session;

    public PushDeliveryService(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Delivers an event to a receiver endpoint using the PUSH delivery method.
     *
     * @param stream The stream configuration
     * @param event The event to deliver
     * @return true if the event was delivered successfully, false otherwise
     */
    public boolean deliverEvent(StreamConfiguration stream, String event) {
        if (stream == null || stream.getDelivery() == null) {
            log.warn("Invalid stream configuration for event delivery");
            return false;
        }
        
        String endpointUrl = stream.getDelivery().getEndpointUrl();
        String authorizationHeader = stream.getDelivery().getAuthorizationHeader();
        
        if (endpointUrl == null || authorizationHeader == null) {
            log.warn("Missing endpoint URL or authorization header for stream " + stream.getStreamId());
            return false;
        }
        
        return deliverEvent(endpointUrl, authorizationHeader, event);
    }

    /**
     * Delivers an event to a receiver endpoint using the PUSH delivery method.
     *
     * @param endpointUrl The endpoint URL to deliver the event to
     * @param authorizationHeader The authorization header to use
     * @param eventToken The event to deliver
     * @return true if the event was delivered successfully, false otherwise
     */
    public boolean deliverEvent(String endpointUrl, String authorizationHeader, String eventToken) {
        try {

            try(var response = createSimpleHttp(endpointUrl, authorizationHeader)
                    .header(HttpHeaders.CONTENT_TYPE, Ssf.APPLICATION_SECEVENT_JWT_TYPE)
                    .entity(new StringEntity(eventToken))
                    .asResponse()) {

                boolean success = response.getStatus() == Response.Status.OK.getStatusCode() ||
                                  response.getStatus() == Response.Status.ACCEPTED.getStatusCode();

                if (!success) {
                    log.warn("Failed to deliver event to " + endpointUrl + ": " + response.getStatus());
                }

                return success;
            }
        } catch (Exception e) {
            log.error("Error delivering event to " + endpointUrl, e);
            return false;
        }
    }

    protected SimpleHttp createSimpleHttp(String endpointUrl, String authorizationHeader) {
        return SimpleHttp.doPost(endpointUrl, session)
                .connectTimeoutMillis(2000)
                .socketTimeOutMillis(3000)
                .auth(authorizationHeader);
    }

}
