package com.identitytailor.keycloak.ssf.streams.model;

import java.net.URI;

public class PollDeliveryMethodRepresentation extends AbstractDeliveryMethodRepresentation {

    public PollDeliveryMethodRepresentation(URI endpointUrl) {
        super(DeliveryMethod.POLL, endpointUrl);
    }
}