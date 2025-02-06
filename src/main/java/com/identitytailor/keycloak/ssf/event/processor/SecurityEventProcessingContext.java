package com.identitytailor.keycloak.ssf.event.processor;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import org.keycloak.models.KeycloakSession;

public class SecurityEventProcessingContext {

    protected KeycloakSession session;

    protected String receiverAlias;

    protected SharedSignalsReceiver receiver;

    protected SecurityEventToken securityEventToken;

    protected boolean processedSuccessfully;

    public SecurityEventToken getSecurityEventToken() {
        return securityEventToken;
    }

    public void setSecurityEventToken(SecurityEventToken securityEventToken) {
        this.securityEventToken = securityEventToken;
    }

    protected void setProcessedSuccessfully(boolean processedSuccessfully) {
        this.processedSuccessfully = processedSuccessfully;
    }

    public boolean isProcessedSuccessfully() {
        return processedSuccessfully;
    }

    public KeycloakSession getSession() {
        return session;
    }

    public void setSession(KeycloakSession session) {
        this.session = session;
    }

    public String getReceiverAlias() {
        return receiverAlias;
    }

    public void setReceiverAlias(String receiverAlias) {
        this.receiverAlias = receiverAlias;
    }

    public SharedSignalsReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(SharedSignalsReceiver receiver) {
        this.receiver = receiver;
    }
}
