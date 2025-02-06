package com.identitytailor.keycloak.ssf.receiver.delivery.push;

import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;
import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.parser.SharedSignalsParsingException;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.Set;

import static com.identitytailor.keycloak.ssf.receiver.util.SharedSignalsResponseUtil.newSharedSignalFailureResponse;
import static org.keycloak.utils.KeycloakSessionUtil.getKeycloakSession;

/**
 * Implements RFC 8935 Push-Based Security Event Token (SET) Delivery Using HTTP
 * <p>
 * https://www.rfc-editor.org/rfc/rfc8935.html
 */
@JBossLog
public class SharedSignalsPushEndpoint {

    public static final String APPLICATION_SECEVENT_JWT_TYPE = "application/secevent+jwt";

    protected final SharedSignalsProvider sharedSignals;

    public SharedSignalsPushEndpoint() {
        sharedSignals = getSharedSignalsProvider();
    }

    protected SharedSignalsProvider getSharedSignalsProvider() {
        return getKeycloakSession().getProvider(SharedSignalsProvider.class);
    }

    @Path("{receiverAlias}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(APPLICATION_SECEVENT_JWT_TYPE) // some SSF providers don't set the correct content-type
    public Response ingestSecurityEventToken(@PathParam("receiverAlias") String receiverAlias, String encodedSecurityEventToken, @HeaderParam(HttpHeaders.AUTHORIZATION) String authToken) {

        KeycloakSession session = getKeycloakSession();
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();
        ReceiverModel receiverModel = new ReceiverManager(session).getReceiverModel(context, receiverAlias);
        if (receiverModel == null) {
            throw newSharedSignalFailureResponse(Response.Status.BAD_REQUEST, SharedSignalsFailureResponse.ERROR_INVALID_REQUEST, "Invalid receiver");
        }

        String pushAuthorizationToken = receiverModel.getPushAuthorizationToken();
        if (pushAuthorizationToken != null) {
            if (!("Bearer " + pushAuthorizationToken).equals(authToken)) {
                throw newSharedSignalFailureResponse(Response.Status.UNAUTHORIZED, SharedSignalsFailureResponse.ERROR_AUTHENTICATION_FAILED, "Invalid auth token");
            }
        }

        // parse security event token
        var processingContext = sharedSignals.createSecurityEventProcessingContext(null, receiverAlias);

        // TODO validate security event token
        SecurityEventToken securityEventToken;
        try {
            securityEventToken = sharedSignals.parseSecurityEventToken(encodedSecurityEventToken, processingContext);
        } catch (SharedSignalsParsingException sepe) {
            // see https://www.rfc-editor.org/rfc/rfc8935.html#section-2.4
            throw newSharedSignalFailureResponse(Response.Status.BAD_REQUEST, SharedSignalsFailureResponse.ERROR_INVALID_REQUEST, sepe.getMessage());
        }

        if (securityEventToken == null) {
            throw newSharedSignalFailureResponse(Response.Status.BAD_REQUEST, SharedSignalsFailureResponse.ERROR_INVALID_REQUEST, "Invalid security event token");
        }
        log.debugf("Ingest security event token. realm=%s receiverAlias=%s jti=%s", realm.getName(), receiverAlias, securityEventToken.getId());

        if (!receiverModel.getIssuer().equals(securityEventToken.getIssuer())) {
            throw newSharedSignalFailureResponse(Response.Status.BAD_REQUEST, SharedSignalsFailureResponse.ERROR_INVALID_ISSUER, "Invalid issuer");
        }

        if (!receiverModel.getAudience().containsAll(Set.of(securityEventToken.getAudience()))) {
            throw newSharedSignalFailureResponse(Response.Status.BAD_REQUEST, SharedSignalsFailureResponse.ERROR_INVALID_AUDIENCE, "Invalid audience");
        }

        processingContext.setSecurityEventToken(securityEventToken);

        // handle security event
        sharedSignals.processSecurityEvents(processingContext);

        if (!processingContext.isProcessedSuccessfully()) {
            // See 2.3. Failure Response https://www.rfc-editor.org/rfc/rfc8935.html#section-2.3
            return Response.serverError().type(MediaType.APPLICATION_JSON).build();
        }

        // See 2.2. Success Response https://www.rfc-editor.org/rfc/rfc8935.html#section-2.2
        return Response.accepted().type(MediaType.APPLICATION_JSON).build();
    }

}
