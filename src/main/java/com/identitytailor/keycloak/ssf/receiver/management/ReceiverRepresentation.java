package com.identitytailor.keycloak.ssf.receiver.management;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiverRepresentation {

    protected String alias;

    protected String componentId;

    protected String description;

    protected String streamId;

    protected Set<String> audience;

    protected Set<String> eventsDelivered;

    protected Boolean managedStream;

    protected String deliveryMethod;

    protected String transmitterUrl;

    protected String transmitterPollUrl;

    protected Integer pollIntervalSeconds;

    protected String receiverPushUrl;

    protected String pushAuthorizationToken;

    protected int configHash;

    protected long modifiedAt;

    protected int maxEvents;

    protected boolean acknowledgeImmediately;
}
