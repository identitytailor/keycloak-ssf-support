package com.identitytailor.keycloak.ssf.receiver.transmitterclient;

import com.identitytailor.keycloak.ssf.SharedSignalsException;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@JBossLog
public class DefaultTransmitterClient implements TransmitterClient {

    private final KeycloakSession session;

    public DefaultTransmitterClient(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public SharedSignalsTransmitterMetadata loadTransmitterMetadata(ReceiverModel receiverModel) {

        SharedSignalsTransmitterMetadata metadata = loadFromCache(receiverModel);

        if (metadata != null) {
            return metadata;
        }

        metadata = fetchTransmitterMetadata(receiverModel);

        if (metadata != null) {
            storeToCache(receiverModel, metadata);
        }

        return metadata;
    }

    @Override
    public SharedSignalsTransmitterMetadata fetchTransmitterMetadata(ReceiverModel receiverModel) {

        RealmModel realm = session.getContext().getRealm();
        String url = receiverModel.getTransmitterConfigUrl();

        log.debugf("Sending transmitter metadata request. realm=%s url=%s", realm.getName(), url);
        SimpleHttp httpCall = prepareHttpCall(url);
        try (var response = httpCall.asResponse()) {
            log.debugf("Received transmitter metadata response. realm=%s status=%s", realm.getName(), response.getStatus());
            if (response.getStatus() != 200) {
                throw new SharedSignalsException("Expected a 200 response but got: " + response.getStatus());
            }
            SharedSignalsTransmitterMetadata metadata = response.asJson(SharedSignalsTransmitterMetadata.class);
            return metadata;
        } catch (Exception e) {
            throw new SharedSignalsException("Could fetch transmitter metadata", e);
        }
    }

    protected void storeToCache(ReceiverModel receiverModel, SharedSignalsTransmitterMetadata metadata) {

        RealmModel realm = session.getContext().getRealm();
        String url = receiverModel.getTransmitterConfigUrl();

        SingleUseObjectProvider cache = session.getProvider(SingleUseObjectProvider.class);
        try {
            String jsonData = JsonSerialization.writeValueAsString(metadata);
            cache.put(makeCacheKey(url), getCacheLifespanSeconds(), Map.of("data", jsonData));
            log.debugf("Stored transmitter metadata in cache. realm=%s url=%s", realm.getName(), url);
        } catch (IOException e) {
            throw new SharedSignalsException("Could not store transmitter metadata in cache", e);
        }
    }

    protected long getCacheLifespanSeconds() {
        return TimeUnit.HOURS.toSeconds(12);
    }

    protected SharedSignalsTransmitterMetadata loadFromCache(ReceiverModel receiverModel) {

        String url = receiverModel.getTransmitterConfigUrl();
        // TODO cache transmitter metadata
        SingleUseObjectProvider cache = session.getProvider(SingleUseObjectProvider.class);
        Map<String, String> cachedTransmitterMetadata = cache.get(makeCacheKey(url));
        if (cachedTransmitterMetadata != null) {
            String jsonData = cachedTransmitterMetadata.get("data");
            try {
                RealmModel realm = session.getContext().getRealm();
                SharedSignalsTransmitterMetadata metadata = JsonSerialization.readValue(jsonData, SharedSignalsTransmitterMetadata.class);
                log.debugf("Loaded transmitter metadata from cache. realm=%s url=%s", realm.getName(), url);
                return metadata;
            } catch (IOException e) {
                throw new SharedSignalsException("Could load transmitter metadata from cache", e);
            }
        }

        return null;
    }

    @Override
    public boolean clearTransmitterMetadata(ReceiverModel receiverModel) {

        SingleUseObjectProvider cache = session.getProvider(SingleUseObjectProvider.class);
        String cacheKey = makeCacheKey(receiverModel.getTransmitterConfigUrl());
        Map<String, String> cachedTransmitterMetadata = cache.get(cacheKey);
        if (cachedTransmitterMetadata != null) {
            cache.remove(cacheKey);
            return true;
        }
        return false;
    }

    protected String makeCacheKey(String url) {
        RealmModel realm = session.getContext().getRealm();
        return "ssf:tm:" + realm.getName() + ":" + url.hashCode();
    }

    protected SimpleHttp prepareHttpCall(String url) {
        return SimpleHttp.doGet(url, session);
    }
}
