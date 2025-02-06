package com.identitytailor.keycloak.ssf.receiver;

public interface SharedSignalsHacks {

    /**
     * HACK: find way to pass current receiver to DefaultSecurityEventParser!!!
     */
    @Deprecated
    String RECEIVER_MODEL_SESSION_ATTRIBUTE = "ssfReceiverModel";
}
