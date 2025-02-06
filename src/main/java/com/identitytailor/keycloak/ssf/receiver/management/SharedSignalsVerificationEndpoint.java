package com.identitytailor.keycloak.ssf.receiver.management;

import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;

import static com.identitytailor.keycloak.ssf.receiver.util.SharedSignalsResponseUtil.newSharedSignalFailureResponse;

@JBossLog
public class SharedSignalsVerificationEndpoint {

    private final KeycloakSession session;

    private final ReceiverManager receiverManager;
    protected final String receiverAlias;

    public SharedSignalsVerificationEndpoint(KeycloakSession session, ReceiverManager receiverManager, String receiverAlias) {
        this.session = session;
        this.receiverManager = receiverManager;
        this.receiverAlias = receiverAlias;
    }

    @POST
    public Response triggerVerification() {

        KeycloakContext context = session.getContext();
        ReceiverModel receiverModel = receiverManager.getReceiverModel(context, receiverAlias);
        if (receiverModel == null) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        SharedSignalsReceiver receiver = receiverManager.lookupReceiver(receiverModel);

        try {
            receiver.requestVerification();
        } catch (Exception e) {
            throw newSharedSignalFailureResponse(Response.Status.INTERNAL_SERVER_ERROR, SharedSignalsFailureResponse.ERROR_INTERNAL_ERROR, e.getMessage());
        }

        return Response.noContent().type(MediaType.APPLICATION_JSON).build();

    }
}
