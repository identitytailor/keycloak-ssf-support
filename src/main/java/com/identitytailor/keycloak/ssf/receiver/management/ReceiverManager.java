package com.identitytailor.keycloak.ssf.receiver.management;

import com.identitytailor.keycloak.ssf.SharedSignalsException;
import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.keys.TransmitterKeyProviderFactory;
import com.identitytailor.keycloak.ssf.keys.TransmitterPublicKeyLoader;
import com.identitytailor.keycloak.ssf.receiver.*;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.streams.model.SharedSignalsStreamRepresentation;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.keys.KeyProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JBossLog
public class ReceiverManager {

    private final KeycloakSession session;

    public ReceiverManager(KeycloakSession session) {
        this.session = session;
    }

    public ReceiverModel createOrUpdateReceiver(KeycloakContext context, String receiverAlias, ReceiverConfig receiverConfig) {

        RealmModel realm = context.getRealm();

        String componentId = createReceiverComponentId(realm, receiverAlias);

        ComponentModel existingComponent = realm.getComponent(componentId);
        ReceiverModel receiverModel;
        if (existingComponent == null) {
            log.infof("Creating new receiver. realm=%s alias=%s", realm.getName(), receiverAlias);
            receiverModel = ReceiverModel.create(receiverAlias, receiverConfig);
            receiverModel.setId(componentId);
            receiverModel.setParentId(realm.getId());
            receiverModel.setName(receiverAlias);
            String providerId = Optional.ofNullable(receiverModel.getProviderId()).orElse("default");
            receiverModel.setProviderId(providerId);
            receiverModel.setProviderType(SharedSignalsReceiver.class.getName());

            realm.addComponentModel(receiverModel);
        } else {
            log.infof("Updating existing receiver. realm=%s alias=%s", realm.getName(), receiverAlias);
            receiverModel = new ReceiverModel(existingComponent);
        }

        SharedSignalsReceiver receiver = lookupReceiver(context, receiverAlias);
        registerKeys(receiverModel);

        if (Boolean.TRUE.equals(receiverModel.getManagedStream())) {
            receiverModel = receiver.registerStream();
            log.debugf("Registered receiver with managed stream. realm=%s alias=%s", realm.getName(), receiverModel.getAlias());
        } else {
            receiverModel = receiver.importStream();
            log.debugf("Registered receiver with pre-configured stream. realm=%s alias=%s", realm.getName(), receiverModel.getAlias());
        }

        updateReceiverModel(realm, receiverModel);

        return receiverModel;
    }

    protected void updateReceiverModel(RealmModel realm, ReceiverModel model) {

        model.setModifiedAt(Time.currentTimeMillis());
        int hash = ReceiverModel.computeConfigHash(model);
        model.setConfigHash(hash);

        realm.updateComponent(model);
    }

    protected ReceiverModel importStreamMetadata(ReceiverModel model) {
        SharedSignalsReceiver receiver = lookupReceiver(model);
        receiver.importStream();
        return receiver.getReceiverModel();
    }

    public void registerKeys(ReceiverModel receiverModel) {

        SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);
        TransmitterClient transmitterClient = sharedSignals.transmitterClient();

        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(receiverModel);

        receiverModel.setIssuer(transmitterMetadata.getIssuer());
        receiverModel.setJwksUri(transmitterMetadata.getJwksUri());

        refreshKeys(session.getContext(), receiverModel, transmitterMetadata);
    }

    protected void refreshKeys(KeycloakContext context, ReceiverModel receiverModel, SharedSignalsTransmitterMetadata transmitterMetadata) {
        RealmModel realm = context.getRealm();
        TransmitterPublicKeyLoader publicKeyLoader = new TransmitterPublicKeyLoader(session, transmitterMetadata);
        try {
            PublicKeysWrapper publicKeysWrapper = publicKeyLoader.loadKeys();
            List<KeyWrapper> keys = publicKeysWrapper.getKeys();
            log.debugf("Fetched %s receiver keys from JWKS url. realm=%s receiver=%s url=%s", keys.size(), realm.getName(), receiverModel.getAlias(), transmitterMetadata.getJwksUri());
            for (var key : keys) {
                createOrUpdateReceiverKey(receiverModel, key, realm);
            }
        } catch (Exception e) {
            throw new SharedSignalsException("Failed to load public keys from transmitter JWKS endpoint", e);
        }
    }

    private static void createOrUpdateReceiverKey(ReceiverModel receiverModel, KeyWrapper key, RealmModel realm) {
        String receiverKeyComponentId = createReceiverKeyComponentId(receiverModel, key.getKid());

        ReceiverKeyModel receiverKeyModel;
        ComponentModel existing = realm.getComponent(receiverKeyComponentId);
        if (existing != null) {
            receiverKeyModel = new ReceiverKeyModel(existing);
        } else {
            receiverKeyModel = new ReceiverKeyModel();
            receiverKeyModel.setId(receiverKeyComponentId);
            receiverKeyModel.setParentId(receiverModel.getId());
            receiverKeyModel.setProviderType(KeyProvider.class.getName());
            receiverKeyModel.setProviderId(TransmitterKeyProviderFactory.PROVIDER_ID);
            String receiverKeyModelName = receiverModel.getName() + " Key Provider " + key.getKid();
            receiverKeyModel.setName(receiverKeyModelName);
        }

        receiverKeyModel.setKid(key.getKid());
        receiverKeyModel.setAlgorithm(key.getAlgorithm());
        receiverKeyModel.setKeyUse(key.getUse());
        receiverKeyModel.setType(key.getType());

        // store public key
        String encodedPublicKey = Base64.getEncoder().encodeToString(key.getPublicKey().getEncoded());
        receiverKeyModel.setPublicKey(encodedPublicKey);

        if (existing == null) {
            realm.addComponentModel(receiverKeyModel);
            log.debugf("Registered receiver key component. realm=%s receiver=%s name='%s'", realm.getName(), receiverModel.getAlias(), receiverKeyModel.getName());
        } else {
            realm.updateComponent(receiverKeyModel);
            log.debugf("Updated receiver key component. realm=%s receiver=%s name='%s'", realm.getName(), receiverModel.getAlias(), receiverKeyModel.getName());
        }
    }

    public void removeReceiver(KeycloakContext context, ReceiverModel receiverModel) {

        SharedSignalsReceiver receiver = lookupReceiver(receiverModel);
        if (receiver == null) {
            return;
        }

        receiver.unregisterStream();
        ReceiverModel model = receiver.getReceiverModel();
        unregisterKeys(model);

        RealmModel realm = context.getRealm();
        realm.removeComponent(model);
        log.debugf("Removed receiver component with id %s. realm=%s receiver=%s", model.getId(), realm.getName(), model.getAlias());
    }

    public void unregisterKeys(ReceiverModel model) {

        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        for (ComponentModel receiverKeyModel : realm.getComponentsStream(model.getId(), TransmitterKeyProviderFactory.PROVIDER_ID).toList()) {
            realm.removeComponent(receiverKeyModel);
            log.debugf("Removed %s receiver key component with id %s. realm=%s receiver=%s", receiverKeyModel.getName(), receiverKeyModel.getId(), realm.getName(), model.getAlias());
        }
    }

    public SharedSignalsReceiver lookupReceiver(KeycloakContext context, String receiverAlias) {

        ReceiverModel receiverModel = getReceiverModel(context, receiverAlias);
        if (receiverModel == null) {
            return null;
        }
        return lookupReceiver(receiverModel);
    }

    public SharedSignalsReceiver lookupReceiver(ReceiverModel receiverModel) {

        KeycloakSessionFactory ksf = session.getKeycloakSessionFactory();
        SharedSignalsReceiverFactory receiverFactory = (SharedSignalsReceiverFactory) ksf.getProviderFactory(SharedSignalsReceiver.class);
        if (receiverFactory == null) {
            return null;
        }

        SharedSignalsReceiver receiver = receiverFactory.create(session, receiverModel);
        return receiver;
    }


    public static String createReceiverComponentId(RealmModel realm, String receiverAlias) {
        String componentId = UUID.nameUUIDFromBytes((realm.getId() + receiverAlias).getBytes()).toString();
        return componentId;
    }

    public static String createReceiverKeyComponentId(ReceiverModel model, String kid) {
        String componentId = UUID.nameUUIDFromBytes((model.getId() + "::" + kid).getBytes()).toString();
        return componentId;
    }

    public List<ReceiverModel> listReceivers(KeycloakContext context) {

        List<ReceiverModel> receiverModels = context.getRealm()
                .getComponentsStream(context.getRealm().getId(), SharedSignalsReceiver.class.getName())
                .map(ReceiverModel::new)
                .toList();

        return receiverModels;
    }

    public ReceiverModel getReceiverModel(KeycloakContext context, String alias) {
        RealmModel realm = context.getRealm();
        String componentId = createReceiverComponentId(realm, alias);
        ComponentModel component = realm.getComponent(componentId);
        if (component != null) {
            return new ReceiverModel(component);
        }
        return null;
    }

    public void refreshReceiver(KeycloakContext context, ReceiverModel receiverModel) {

        SharedSignalsTransmitterMetadata transmitterMetadata = refreshTransmitterMetadata(receiverModel);
        refreshKeys(context, receiverModel, transmitterMetadata);
        ReceiverModel updatedModel = refreshStream(receiverModel);

        RealmModel realm = context.getRealm();
        updateReceiverModel(realm, updatedModel);

        log.debugf("Refreshed receiver model. realm=%s receiver=%s", realm.getName(), receiverModel.getAlias());
    }

    public ReceiverModel refreshStream(ReceiverModel receiverModel) {
        ReceiverModel updatedModel = importStreamMetadata(receiverModel);
        return updatedModel;
    }

    public SharedSignalsTransmitterMetadata refreshTransmitterMetadata(ReceiverModel receiverModel) {

        SharedSignalsReceiver receiver = lookupReceiver(receiverModel);
        if (receiver == null) {
            return null;
        }

        return receiver.refreshTransmitterMetadata();
    }
}
