package com.identitytailor.keycloak.ssf.transmitter.storage.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA entity for storing SSF events in a relational database.
 */
@Entity
@Table(name = "SSF_EVENT")
@Data
public class SsfEventEntity {
    
    @Id
    private String id;

    @Column(name = "REALM_ID")
    private String realmId;
    
    @Column(name = "STREAM_ID")
    private String streamId;
    
    @Column(name = "EVENT_DATA", nullable = false, columnDefinition = "TEXT")
    private String eventData;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Long createdAt;
    
    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column
    private Boolean failed = false;
    
    @Column(name = "SEQUENCE_ORDER", nullable = false)
    private Long sequenceOrder;
}
