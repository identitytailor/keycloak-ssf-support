package com.identitytailor.keycloak.ssf.receiver;

import com.identitytailor.keycloak.ssf.streams.model.DeliveryMethod;
import lombok.Data;

import java.util.Set;

@Data
public class ReceiverConfig {

    protected String alias;

    protected String description;

    protected String transmitterUrl;

    protected String transmitterConfigUrl;

    protected String transmitterPollUrl;

    protected String transmitterAccessToken;

    protected Boolean managedStream;

    protected DeliveryMethod deliveryMethod;

    protected String pushAuthorizationToken;

    protected String receiverPushUrl;

    protected int pollIntervalSeconds;

    protected Set<String> eventsRequested;

    protected String providerId;

    protected String streamId;
}
