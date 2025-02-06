package com.identitytailor.keycloak.ssf.storage.jpa;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.storage.SharedSignalsEventStore;
import jakarta.persistence.EntityManager;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.UUID;

public class JpaSharedSignalsEventStore implements SharedSignalsEventStore {

    private final EntityManager entityManager;

    public JpaSharedSignalsEventStore(KeycloakSession session) {
        this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @Override
    public void storeSecurityEvent(RealmModel realm, ReceiverModel receiverModel, SecurityEventToken securityEvent) {

        SecurityEventEntity securityEventEntity = new SecurityEventEntity();
        securityEventEntity.setId(UUID.randomUUID().toString());
        securityEventEntity.setRealmId(realm.getId());
        securityEventEntity.setCreatedTimestamp(Time.currentTimeMillis());
        securityEventEntity.setReceiverAlias(receiverModel.getAlias());
        try {
            securityEventEntity.setSecurityEventJson(JsonSerialization.writeValueAsString(securityEvent));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        entityManager.persist(securityEventEntity);
    }
}
