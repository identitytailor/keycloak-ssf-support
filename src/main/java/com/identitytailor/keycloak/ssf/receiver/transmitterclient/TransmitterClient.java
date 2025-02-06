package com.identitytailor.keycloak.ssf.receiver.transmitterclient;

import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;

public interface TransmitterClient {

    SharedSignalsTransmitterMetadata loadTransmitterMetadata(ReceiverModel receiverModel);

    SharedSignalsTransmitterMetadata fetchTransmitterMetadata(ReceiverModel receiverModel);

    boolean clearTransmitterMetadata(ReceiverModel receiverModel);
}
