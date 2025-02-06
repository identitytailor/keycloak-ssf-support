package com.identitytailor.keycloak.ssf.receiver;

import org.keycloak.component.ComponentFactory;

public interface SharedSignalsReceiverFactory extends ComponentFactory<SharedSignalsReceiver, SharedSignalsReceiver> {

    @Override
    default void close() {

    }
}
