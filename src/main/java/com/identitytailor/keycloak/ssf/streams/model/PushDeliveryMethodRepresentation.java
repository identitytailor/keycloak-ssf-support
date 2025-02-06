package com.identitytailor.keycloak.ssf.streams.model;

import java.net.URI;
import java.util.Objects;

public class PushDeliveryMethodRepresentation extends AbstractDeliveryMethodRepresentation {

    /**
     * @param endpointUrl MUST be supplied by the Receiver
     */
    public PushDeliveryMethodRepresentation(URI endpointUrl) {
        super(DeliveryMethod.PUSH, Objects.requireNonNull(endpointUrl, "endpointUrl"));
    }
}