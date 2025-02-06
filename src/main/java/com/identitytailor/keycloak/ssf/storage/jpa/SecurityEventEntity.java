package com.identitytailor.keycloak.ssf.storage.jpa;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="SECURITY_EVENT_ENTITY")
public class SecurityEventEntity {

    @Id
    @Column(name="ID", length = 36)
    protected String id;

    @Column(name = "REALM_ID")
    protected String realmId;

    @Column(name = "RECEIVER_ALIAS")
    protected String receiverAlias;

    @Column(name = "CREATED_TIMESTAMP")
    protected long createdTimestamp;

    @Column(name = "ACK_TIMESTAMP")
    protected long acknowledgedTimestamp;

    @Column(name = "SECURITY_EVENT_JSON")
    protected String securityEventJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getReceiverAlias() {
        return receiverAlias;
    }

    public void setReceiverAlias(String receiverAlias) {
        this.receiverAlias = receiverAlias;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getSecurityEventJson() {
        return securityEventJson;
    }

    public void setSecurityEventJson(String securityEventJsonBlob) {
        this.securityEventJson = securityEventJsonBlob;
    }

    public long getAcknowledgedTimestamp() {
        return acknowledgedTimestamp;
    }

    public void setAcknowledgedTimestamp(long acknowledgedTimestamp) {
        this.acknowledgedTimestamp = acknowledgedTimestamp;
    }
}
