package com.identitytailor.keycloak.ssf.receiver;

import com.identitytailor.keycloak.ssf.streams.model.DeliveryMethod;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;

import java.util.*;

public class ReceiverModel extends ComponentModel {

    public ReceiverModel() {
    }

    public ReceiverModel(ComponentModel model) {
        super(model);
    }

    public static ReceiverModel create(String alias, ReceiverConfig config) {

        ReceiverModel model = new ReceiverModel();
        model.setAlias(alias);
        model.setDescription(config.getDescription());

        model.setTransmitterAccessToken(config.getTransmitterAccessToken());
        if (config.getPushAuthorizationToken() != null) {
            model.setPushAuthorizationToken(config.getPushAuthorizationToken());
        }

        String transmitterUrl = Objects.requireNonNull(config.getTransmitterUrl(), "transmitterUrl");
        model.setTransmitterUrl(transmitterUrl);

        String transmitterConfigUrl = config.getTransmitterConfigUrl();
        if (transmitterConfigUrl == null) {
            String configUrl = transmitterUrl;
            if (!configUrl.endsWith("/")) {
                configUrl+="/";
            }
            configUrl = configUrl + ".well-known/ssf-configuration";
            transmitterConfigUrl = configUrl;
        }
        model.setTransmitterConfigUrl(transmitterConfigUrl);

        model.setTransmitterPollUrl(config.getTransmitterPollUrl());
        model.setPollIntervalSeconds(config.getPollIntervalSeconds());
        model.setManagedStream(config.getManagedStream());

        if (Boolean.TRUE.equals(model.getManagedStream())) {
            model.setEventsRequested(config.getEventsRequested());
            model.setDeliveryMethod(config.getDeliveryMethod());
        } else {
            String streamId = Objects.requireNonNull(config.getStreamId(), "streamId");
            model.setStreamId(streamId);
        }

        return model;
    }

    public void setIssuer(String issuer) {
        getConfig().putSingle("issuer", issuer);
    }

    public String getIssuer() {
        return getConfig().getFirst("issuer");
    }

    public void setJwksUri(String issuer) {
        getConfig().putSingle("jwksUri", issuer);
    }

    public String getJwksUri() {
        return getConfig().getFirst("jwksUri");
    }

    public String getStreamId() {
        return getConfig().getFirst("streamId");
    }

    public void setStreamId(String streamId) {
        getConfig().putSingle("streamId", streamId);
    }

    public String getTransmitterUrl() {
        return getConfig().getFirst("transmitterUrl");
    }

    public void setTransmitterUrl(String transmitterUrl) {
        getConfig().putSingle("transmitterUrl", transmitterUrl);
    }

    public String getTransmitterConfigUrl() {
        return getConfig().getFirst("transmitterConfigUrl");
    }

    public void setTransmitterConfigUrl(String transmitterConfigUrl) {
        getConfig().putSingle("transmitterConfigUrl", transmitterConfigUrl);
    }

    public String getTransmitterPollUrl() {
        return getConfig().getFirst("transmitterPollUrl");
    }

    public void setTransmitterPollUrl(String transmitterPollUrl) {
        getConfig().putSingle("transmitterPollUrl", transmitterPollUrl);
    }

    public String getReceiverPushUrl() {
        return getConfig().getFirst("receiverPushUrl");
    }

    public void setReceiverPushUrl(String receiverPushUrl) {
        getConfig().putSingle("receiverPushUrl", receiverPushUrl);
    }

    public DeliveryMethod getDeliveryMethod() {
        return DeliveryMethod.valueOf(getConfig().getFirst("deliveryMethod"));
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        getConfig().putSingle("deliveryMethod", deliveryMethod.name());
    }

    public Boolean getManagedStream() {
        return Boolean.valueOf(getConfig().getFirst("managedStream"));
    }

    public void setManagedStream(Boolean managedStream) {
        getConfig().putSingle("managedStream", Boolean.toString(Boolean.TRUE.equals(managedStream)));
    }

    public Integer getPollIntervalSeconds() {
        String pollIntervalSeconds = getConfig().getFirst("pollIntervalSeconds");
        if (pollIntervalSeconds == null || pollIntervalSeconds.isEmpty()) {
            return null;
        }

        return Integer.parseInt(pollIntervalSeconds);
    }

    public void setPollIntervalSeconds(Integer pollIntervalSeconds) {
        if (pollIntervalSeconds != null) {
            getConfig().putSingle("pollIntervalSeconds", Integer.toString(pollIntervalSeconds));
        }
    }

    public String getTransmitterAccessToken() {
        return getConfig().getFirst("transmitterAccessToken");
    }

    public void setTransmitterAccessToken(String transmitterAccessToken) {
        getConfig().putSingle("transmitterAccessToken", transmitterAccessToken);
    }

    public String getDescription() {
        return getConfig().getFirst("description");
    }

    public void setDescription(String description) {
        getConfig().putSingle("description", description);
    }

    public Set<String> getEventsRequested() {
        List<String> eventsRequested = getConfig().getList("eventsRequested");
        if (eventsRequested == null || eventsRequested.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(new TreeSet<>(eventsRequested));
    }

    public void setEventsRequested(Set<String> eventsRequested) {
        getConfig().put("eventsRequested", eventsRequested.stream().toList());
    }

    public Set<String> getEventsDelivered() {
        List<String> eventsDelivered = getConfig().getList("eventsDelivered");
        if (eventsDelivered == null || eventsDelivered.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(new TreeSet<>(eventsDelivered));
    }

    public void setEventsDelivered(Set<String> eventsDelivered) {
        getConfig().put("eventsDelivered", eventsDelivered.stream().toList());
    }

    public String getAlias() {
        return getConfig().getFirst("alias");
    }

    public void setAlias(String alias) {
        getConfig().putSingle("alias", alias);
    }

    public boolean isPollDelivery() {
        return DeliveryMethod.POLL.equals(getDeliveryMethod());
    }

    public void setAudience(Set<String> audience) {
        getConfig().put("audience", new ArrayList<>(audience));
    }

    public Set<String> getAudience() {
        List<String> audience = getConfig().getList("audience");
        if (audience == null || audience.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(audience);
    }

    public void setModifiedAt(long timestamp) {
        getConfig().putSingle("modifiedAt", Long.toString(timestamp));
    }

    public long getModifiedAt() {
        String modifiedAt = getConfig().getFirst("modifiedAt");
        if (modifiedAt == null || modifiedAt.isEmpty()) {
            return -1L;
        }
        return Long.parseLong(modifiedAt);
    }

    public static int computeConfigHash(ReceiverModel receiverModel) {
        var copy = new MultivaluedHashMap<>(receiverModel.getConfig());
        copy.remove("modifiedAt");
        copy.remove("configHash");
        return copy.hashCode();
    }

    public int getConfigHash() {
        String configHash = getConfig().getFirst("configHash");
        return Integer.parseInt(configHash);
    }

    public void setConfigHash(int configHash) {
        getConfig().putSingle("configHash", Integer.toString(configHash));
    }

    public void setPushAuthorizationToken(String pushAuthorizationToken) {
        getConfig().putSingle("pushAuthorizationToken", pushAuthorizationToken);
    }

    public String getPushAuthorizationToken() {
        return getConfig().getFirst("pushAuthorizationToken");
    }
}
