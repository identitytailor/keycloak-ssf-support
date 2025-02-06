package com.identitytailor.keycloak.ssf.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.identitytailor.keycloak.ssf.event.subjects.SubjectIdJsonDeserializer;
import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;
import com.identitytailor.keycloak.ssf.event.types.SecurityEventMapJsonDeserializer;
import com.identitytailor.keycloak.ssf.event.subjects.SubjectId;
import org.keycloak.representations.JsonWebToken;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecurityEventToken extends JsonWebToken {

    @JsonProperty("sub_id")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId subjectId;

    @JsonProperty("txn")
    protected String txn;

    @JsonProperty("events")
    @JsonDeserialize(using = SecurityEventMapJsonDeserializer.class)
    protected Map<String, SecurityEvent> events;

    public SecurityEventToken txn(String txn) {
        setTxn(txn);
        return this;
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(SubjectId subjectId) {
        this.subjectId = subjectId;
    }

    public SecurityEventToken subjectId(SubjectId subjectId) {
        setSubjectId(subjectId);
        return this;
    }

    public Map<String, SecurityEvent> getEvents() {
        if (events == null) {
            events = new LinkedHashMap<>();
        }
        return events;
    }

    public void setEvents(Map<String, SecurityEvent> events) {
        this.events = events;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
