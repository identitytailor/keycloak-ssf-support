package com.identitytailor.keycloak.ssf.receiver.streamclient;

import com.identitytailor.keycloak.ssf.streams.model.CreateStreamRequest;
import com.identitytailor.keycloak.ssf.streams.model.SharedSignalsStreamRepresentation;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;

public interface SharedSignalsStreamClient {

    SharedSignalsStreamRepresentation createStream(SharedSignalsTransmitterMetadata transmitterMetadata, String transmitterAccessToken, CreateStreamRequest request);

    void deleteStream(SharedSignalsTransmitterMetadata transmitterMetadata, String authorizationToken, String streamId);

    SharedSignalsStreamRepresentation getStream(SharedSignalsTransmitterMetadata transmitterMetadata, String authorizationToken, String streamId);
}
