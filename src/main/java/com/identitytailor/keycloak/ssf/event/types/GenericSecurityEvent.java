package com.identitytailor.keycloak.ssf.event.types;

public class GenericSecurityEvent extends SecurityEvent {

    public GenericSecurityEvent() {
        super(null);
    }

    @Override
    public String toString() {
        return "GenericSecurityEvent{" +
               "subjectId=" + subjectId +
               ", eventType='" + eventType + '\'' +
               ", eventTimestamp=" + eventTimestamp +
               ", initiatingEntity=" + initiatingEntity +
               ", reasonAdmin=" + reasonAdmin +
               ", reasonUser=" + reasonUser +
               ", attributes=" + attributes +
               '}';
    }
}
