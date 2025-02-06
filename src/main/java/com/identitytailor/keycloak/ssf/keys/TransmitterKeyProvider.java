package com.identitytailor.keycloak.ssf.keys;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.keys.KeyProvider;
import org.keycloak.models.KeycloakSession;

import java.util.stream.Stream;

/**
 * Dummy class used in combination with ReceiverKey ComponentModels
 */
@JBossLog
public class TransmitterKeyProvider implements KeyProvider {

    private final KeycloakSession session;

    private final ComponentModel model;

    public TransmitterKeyProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public Stream<KeyWrapper> getKeysStream() {
        return Stream.empty();
    }

}
