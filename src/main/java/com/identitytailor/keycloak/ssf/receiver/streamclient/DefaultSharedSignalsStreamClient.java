package com.identitytailor.keycloak.ssf.receiver.streamclient;

import com.identitytailor.keycloak.ssf.streams.model.CreateStreamRequest;
import com.identitytailor.keycloak.ssf.streams.model.SharedSignalsStreamRepresentation;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Map;

@JBossLog
public class DefaultSharedSignalsStreamClient implements SharedSignalsStreamClient {

    private final KeycloakSession session;

    public DefaultSharedSignalsStreamClient(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public SharedSignalsStreamRepresentation createStream(
            SharedSignalsTransmitterMetadata transmitterMetadata,
            String transmitterAccessToken,
            CreateStreamRequest createStreamRequest) {

        try {
            log.debugf("Sending stream creation request. %s", JsonSerialization.writeValueAsPrettyString(createStreamRequest));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String uri = transmitterMetadata.getConfigurationEndpoint();
        SimpleHttp httpCall = SimpleHttp.doPost(uri, session).auth(transmitterAccessToken).json(createStreamRequest);
        try (var response = httpCall.asResponse()) {
            log.debugf("Stream creation response. status=%s", response.getStatus());

            if (response.getStatus() != 201) {
                log.errorf("Stream creation failed. %s", response.asJson(Map.class));
                throw new SharedSignalsStreamException("Expected a 201 response but got: " + response.getStatus(), Response.Status.fromStatusCode(response.getStatus()));
            }

            return response.asJson(SharedSignalsStreamRepresentation.class);
        } catch (IOException ioe) {
            throw new SharedSignalsStreamException("I/O error during stream creation", ioe, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteStream(SharedSignalsTransmitterMetadata transmitterMetadata, String authorizationToken, String streamId) {

        RealmModel realm = session.getContext().getRealm();
        log.debugf("Sending stream deletion request. realm=%s stream_id=%s", realm.getName(), streamId);

        String uri = transmitterMetadata.getConfigurationEndpoint() + "?stream_id=" + streamId;
        SimpleHttp httpCall = SimpleHttp.doDelete(uri, session).auth(authorizationToken);
        try (var response = httpCall.asResponse()) {
            log.debugf("Stream deletion response. status=%s", response.getStatus());

            if (response.getStatus() != 204) {
                log.errorf("Stream deletion failed. realm=%s stream_id=%s error='%s'", realm.getName(), streamId, response.asJson(Map.class));
                throw new SharedSignalsStreamException("Expected a 204 response but got: " + response.getStatus(), Response.Status.fromStatusCode(response.getStatus()));
            }
        } catch (Exception e) {
            throw new SharedSignalsStreamException("Could not send stream deletion request", e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SharedSignalsStreamRepresentation getStream(SharedSignalsTransmitterMetadata transmitterMetadata, String authorizationToken, String streamId) {

        RealmModel realm = session.getContext().getRealm();
        log.debugf("Sending stream read request. realm=%s stream_id=%s", realm.getName(), streamId);

        String uri = transmitterMetadata.getConfigurationEndpoint() + "?stream_id=" + streamId;
        SimpleHttp httpCall = SimpleHttp.doGet(uri, session).auth(authorizationToken);
        try (var response = httpCall.asResponse()) {
            log.debugf("Stream read response. status=%s", response.getStatus());

            if (response.getStatus() != 200) {
                log.errorf("Stream read request failed. realm=%s stream_id=%s error='%s'", realm.getName(), streamId, response.asJson(Map.class));
                throw new SharedSignalsStreamException("Expected a 200 response but got: " + response.getStatus(), Response.Status.fromStatusCode(response.getStatus()));
            }

            return response.asJson(SharedSignalsStreamRepresentation.class);
        } catch (Exception e) {
            throw new SharedSignalsStreamException("Could not send stream read request", e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
