package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsFailureResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SecurityEventPollingContext {

    protected Set<String> securityEventIdsToAcknowledge = new HashSet<>();

    protected Map<String, Object> errors = new HashMap<>();

    public void registerSecurityEventForAcknowledgment(String jti) {
        securityEventIdsToAcknowledge.add(jti);
    }

    public Set<String> getSecurityEventIdsToAcknowledge() {
        return securityEventIdsToAcknowledge;
    }

    public void setSecurityEventIdsToAcknowledge(Set<String> securityEventIdsToAcknowledge) {
        this.securityEventIdsToAcknowledge = securityEventIdsToAcknowledge;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }

    public boolean hasSecurityEventsToAcknowledge() {
        return !securityEventIdsToAcknowledge.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void registerSecurityEventForError(String id, SharedSignalsFailureResponse failureResponse) {
        securityEventIdsToAcknowledge.remove(id);
        errors.put(id, failureResponse);
    }

    public void markSecurityEventAsAcknowledged() {
        securityEventIdsToAcknowledge.clear();
    }

    public void markSecurityEventAsAcknowledged(Set<String> ack) {
        if (ack == null) {
            return;
        }
        securityEventIdsToAcknowledge.removeAll(ack);
    }
}
