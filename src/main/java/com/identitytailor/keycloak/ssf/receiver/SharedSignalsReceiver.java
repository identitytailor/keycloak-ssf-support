package com.identitytailor.keycloak.ssf.receiver;

import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.provider.Provider;

import java.util.stream.Stream;

public interface SharedSignalsReceiver extends Provider {

    @Override
    default void close() {
    }

    Stream<KeyWrapper> getKeys();

    ReceiverModel getReceiverModel();

    ReceiverModel registerStream();

    ReceiverModel importStream();

    void unregisterStream();

    SharedSignalsTransmitterMetadata refreshTransmitterMetadata();

    void requestVerification();
}
