package com.identitytailor.keycloak.ssf.receiver;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.keys.TransmitterKeyManager;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.receiver.verification.VerificationState;
import com.identitytailor.keycloak.ssf.storage.VerificationStore;
import com.identitytailor.keycloak.ssf.streams.model.DeliveryMethod;
import com.identitytailor.keycloak.ssf.streams.model.SharedSignalsStreamRepresentation;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.keys.KeyProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.net.URI;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JBossLog
public class DefaultSharedSignalsReceiver implements SharedSignalsReceiver {

    protected final KeycloakSession session;

    protected final SharedSignalsProvider sharedSignals;

    protected final ReceiverModel receiverModel;

    public DefaultSharedSignalsReceiver(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.sharedSignals = session.getProvider(SharedSignalsProvider.class);
        if (model instanceof ReceiverModel rm) {
            this.receiverModel = rm;
        } else {
            this.receiverModel = new ReceiverModel(model);
        }
    }

    public DefaultSharedSignalsReceiver(KeycloakSession session) {
        this(session, new ComponentModel());
    }

    @Override
    public ReceiverModel getReceiverModel() {
        return receiverModel;
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Stream<KeyWrapper> getKeys() {

        RealmModel realm = session.getContext().getRealm();

        return realm.getComponentsStream(receiverModel.getId(), KeyProvider.class.getName()).map(ReceiverKeyModel::new).map(receiverKey -> {
            String encodedPublicKey = receiverKey.getPublicKey();
            PublicKey publicKey = TransmitterKeyManager.decodePublicKey(encodedPublicKey, receiverKey.getType(), receiverKey.getAlgorithm());
            KeyWrapper key = new KeyWrapper();
            key.setKid(receiverKey.getKid());
            key.setAlgorithm(receiverKey.getAlgorithm());
            key.setUse(receiverKey.getKeyUse());
            key.setType(receiverKey.getType());
            key.setPublicKey(publicKey);
            return key;
        });
    }

    @Override
    public SharedSignalsTransmitterMetadata refreshTransmitterMetadata() {

        TransmitterClient transmitterClient = sharedSignals.transmitterClient();

        RealmModel realm = session.getContext().getRealm();
        boolean cleared = transmitterClient.clearTransmitterMetadata(receiverModel);
        if (cleared) {
            log.debugf("Cleared Transmitter metadata. realm=%s receiver=%s", realm.getName(), receiverModel.getAlias());
        }

        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(receiverModel);

        log.debugf("Refreshed Transmitter metadata. realm=%s receiver=%s", realm.getName(), receiverModel.getAlias());

        return transmitterMetadata;
    }

    @Override
    public void unregisterStream() {
        try {
            if (Boolean.TRUE.equals(receiverModel.getManagedStream())) {
                RealmModel realm = session.getContext().getRealm();
                sharedSignals.receiverStreamManager().deleteReceiverStream(receiverModel);
                log.debugf("Removed managed stream for receiver component with id %s. realm=%s alias=%s stream_id=%s", realm.getName(), receiverModel.getId(), receiverModel.getAlias(), receiverModel.getStreamId());
            }
        } catch (Exception e) {
            log.errorf("Could not delete receiver stream with id %s. alias=%s", receiverModel.getId(), receiverModel.getAlias());
        }
    }

    @Override
    public ReceiverModel registerStream() {

        SharedSignalsStreamRepresentation streamRep = sharedSignals.receiverStreamManager().createReceiverStream(session.getContext(), receiverModel);
        updateReceiverModelFromStreamRepresentation(streamRep);

        return receiverModel;
    }

    @Override
    public ReceiverModel importStream() {

        SharedSignalsStreamRepresentation streamRep = sharedSignals.receiverStreamManager().getStream(receiverModel);
        updateReceiverModelFromStreamRepresentation(streamRep);

        return receiverModel;
    }

    protected void updateReceiverModelFromStreamRepresentation(SharedSignalsStreamRepresentation streamRep) {

        receiverModel.setStreamId(streamRep.getId());
        receiverModel.setIssuer(streamRep.getIssuer().toString());

        Object audience = streamRep.getAudience();
        if (audience != null) {
            if (audience instanceof String audienceString) {
                receiverModel.setAudience(Set.of(audienceString));
            } else if (audience instanceof Collection<?> audienceColl) {
                receiverModel.setAudience(Set.copyOf((Collection<String>) audienceColl));
            }
        }

        DeliveryMethod deliveryMethod = streamRep.getDelivery().getMethod();
        receiverModel.setDeliveryMethod(deliveryMethod);
        switch(deliveryMethod) {
            case PUSH -> {
                receiverModel.setPushAuthorizationToken(streamRep.getDelivery().getAuthorizationHeader());
                receiverModel.setReceiverPushUrl(streamRep.getDelivery().getEndpointUrl().toString());
            }
            case POLL -> {
                receiverModel.setTransmitterPollUrl(streamRep.getDelivery().getEndpointUrl().toString());
            }
        }

        receiverModel.setEventsDelivered(streamRep.getEventsDelivered().stream().map(URI::toString).collect(Collectors.toSet()));
        if (receiverModel.getDescription() == null) {
            receiverModel.setDescription(streamRep.getDescription());
        }
    }

    @Override
    public void requestVerification() {

        VerificationStore storage = sharedSignals.verificationStore();

        // store current verification state
        RealmModel realm = session.getContext().getRealm();
        VerificationState verificationState = storage.getVerificationState(realm, receiverModel);
        if (verificationState != null) {
            log.debugf("Resetting pending verification state for stream. %s", verificationState);
            storage.clearVerificationState(realm, receiverModel);
        }

        TransmitterClient transmitterClient = sharedSignals.transmitterClient();
        SharedSignalsTransmitterMetadata transmitterMetadata = transmitterClient.loadTransmitterMetadata(receiverModel);
        String state = UUID.randomUUID().toString();

        sharedSignals.verificationClient().requestVerification(receiverModel, transmitterMetadata, state);

        // store current verification state
        storage.setVerificationState(realm, receiverModel, state);
    }
}
