package com.identitytailor.keycloak.ssf.receiver.management;

import com.identitytailor.keycloak.ssf.receiver.ReceiverConfig;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.streamclient.SharedSignalsStreamException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;

import java.util.List;
import java.util.Map;

import static com.identitytailor.keycloak.ssf.receiver.util.SharedSignalsResponseUtil.newSharedSignalFailureResponse;

@JBossLog
public class ReceiverManagementEndpoint {

    private final KeycloakSession session;

    private final ReceiverManager receiverManager;

    public ReceiverManagementEndpoint(KeycloakSession session, ReceiverManager receiverManager) {
        this.session = session;
        this.receiverManager = receiverManager;
    }

    /**
     * @param alias
     * @param config
     * @return
     */
    @PUT
    @Path("/receivers/{receiverAlias}")
    public Response updateReceiverConfig(@PathParam("receiverAlias") String alias, ReceiverConfig config) {

        ReceiverModel receiverModel;
        try {
            receiverModel = receiverManager.createOrUpdateReceiver(session.getContext(), alias, config);
        } catch (SharedSignalsStreamException ssse) {
            throw newSharedSignalFailureResponse(ssse.getStatus(), ssse.getStatus().getReasonPhrase(), "Could not update receiver config: "+ ssse.getMessage());
        } catch (Exception e) {
            throw newSharedSignalFailureResponse(Response.Status.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Could not update receiver config: " + e.getMessage());
        }

        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(modelToRep(receiverModel)).build();
    }

    @POST
    @Path("/receivers/{receiverAlias}/refresh")
    public Response refreshReceiver(@PathParam("receiverAlias") String alias) {

        KeycloakContext context = session.getContext();
        ReceiverModel receiverModel = receiverManager.getReceiverModel(context, alias);
        if (receiverModel == null) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        receiverManager.refreshReceiver(context, receiverModel);

        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(Map.of("status", "refreshed")).build();
    }

    @Path("/receivers/{receiverAlias}/verify")
    public SharedSignalsVerificationEndpoint verificationEndpoint(@PathParam("receiverAlias") String alias) {
        return new SharedSignalsVerificationEndpoint(session, receiverManager, alias);
    }

    @DELETE
    @Path("/receivers/{receiverAlias}")
    public Response deleteReceiverConfig(@PathParam("receiverAlias") String alias) {

        KeycloakContext context = session.getContext();
        ReceiverModel receiverModel = receiverManager.getReceiverModel(context, alias);
        if (receiverModel == null) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        receiverManager.removeReceiver(context, receiverModel);

        return Response.noContent().type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/receivers")
    public Response listReceivers() {

        List<ReceiverModel> receiverModels = receiverManager.listReceivers(session.getContext());
        List<ReceiverRepresentation> reps = receiverModels.stream().map(this::modelToRep).toList();
        return Response.ok().entity(reps).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/receivers/{receiverAlias}")
    public Response getReceiver(@PathParam("receiverAlias") String alias) {

        ReceiverModel receiverModel = receiverManager.getReceiverModel(session.getContext(), alias);
        if (receiverModel == null) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        return Response.ok().entity(modelToRep(receiverModel)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    protected ReceiverRepresentation modelToRep(ReceiverModel model) {
        ReceiverRepresentation rep = new ReceiverRepresentation();

        rep.setComponentId(model.getId());
        rep.setAlias(model.getAlias());
        rep.setDescription(model.getDescription());
        rep.setAudience(model.getAudience());
        rep.setManagedStream(model.getManagedStream());
        rep.setEventsDelivered(model.getEventsDelivered());
        rep.setPollIntervalSeconds(model.getPollIntervalSeconds());
        rep.setPushAuthorizationToken(model.getPushAuthorizationToken());
        rep.setTransmitterUrl(model.getTransmitterUrl());
        rep.setTransmitterPollUrl(model.getTransmitterPollUrl());
        rep.setReceiverPushUrl(model.getReceiverPushUrl());
        rep.setDeliveryMethod(model.getDeliveryMethod().name());
        rep.setStreamId(model.getStreamId());
        rep.setModifiedAt(model.getModifiedAt());
        rep.setConfigHash(model.getConfigHash());
        rep.setMaxEvents(model.getMaxEvents());
        rep.setAcknowledgeImmediately(model.isAcknowledgeImmediately());
        return rep;
    }
}
