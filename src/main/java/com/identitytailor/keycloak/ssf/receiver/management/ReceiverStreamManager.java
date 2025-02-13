package com.identitytailor.keycloak.ssf.receiver.management;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.streamclient.SharedSignalsStreamClient;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.streams.model.CreateStreamRequest;
import com.identitytailor.keycloak.ssf.streams.model.PollDeliveryMethodRepresentation;
import com.identitytailor.keycloak.ssf.streams.model.PushDeliveryMethodRepresentation;
import com.identitytailor.keycloak.ssf.streams.model.SharedSignalsStreamRepresentation;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.services.Urls;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.net.URI;

@JBossLog
public class ReceiverStreamManager {

    protected final SharedSignalsStreamClient streamClient;

    protected final TransmitterClient transmitterClient;

    public ReceiverStreamManager(SharedSignalsProvider sharedSignals) {
        this.streamClient = sharedSignals.streamClient();
        this.transmitterClient = sharedSignals.transmitterClient();
    }

    public SharedSignalsStreamRepresentation createReceiverStream(KeycloakContext context, ReceiverModel model) {

        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(model);
        CreateStreamRequest createStreamRequest = createCreateStreamRequest(context, model);
        SharedSignalsStreamRepresentation streamRep = streamClient.createStream(transmitterMetadata, model.getTransmitterAccessToken(), createStreamRequest);

        try {
            log.infof("Created stream rep: %s", JsonSerialization.writeValueAsPrettyString(streamRep));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // update streamId
        model.setStreamId(streamRep.getId());
        context.getRealm().updateComponent(model);

        return streamRep;
    }

    protected CreateStreamRequest createCreateStreamRequest(KeycloakContext context, ReceiverModel model) {

        CreateStreamRequest createStreamRequest = new CreateStreamRequest();
        createStreamRequest.setDescription(model.getDescription());
        createStreamRequest.setEventsRequested(model.getEventsRequested());
        switch(model.getDeliveryMethod()) {
            case POLL -> createStreamRequest.setDelivery(new PollDeliveryMethodRepresentation(null));
            case PUSH -> {
                String pushUrl = createPushUrl(context, model);
                createStreamRequest.setDelivery(new PushDeliveryMethodRepresentation(URI.create(pushUrl), model.getPushAuthorizationToken()));
            }
        }

        return createStreamRequest;
    }

    public String createPushUrl(KeycloakContext context, ReceiverModel model) {
        String issuer = Urls.realmIssuer(context.getUri().getBaseUri(), context.getRealm().getName());
        String pushUrl = issuer + "/ssf/push/" + model.getAlias();
        return pushUrl;
    }

    public void deleteReceiverStream(ReceiverModel model) {

        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(model);
        streamClient.deleteStream(transmitterMetadata, model.getTransmitterAccessToken(), model.getStreamId());
    }

    public SharedSignalsStreamRepresentation getStream(ReceiverModel model) {

        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(model);
        SharedSignalsStreamRepresentation streamRep = streamClient.getStream(transmitterMetadata, model.getTransmitterAccessToken(), model.getStreamId());
        return streamRep;
    }

}
