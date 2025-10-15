package com.identitytailor.keycloak.ssf.transmitter.storage.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA entity for storing SSF stream configurations in a relational database.
 */
@Entity
@Table(name = "SSF_STREAM")
@Data
public class SsfStreamEntity {
    
    @Id
    private String id;

    @Column(name = "REALM_ID")
    private String realmId;

    private String description;
    
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String statusReason;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Integer createdAt;
    
    @Column(name = "UPDATED_AT", nullable = false)
    private Integer updatedAt;
    
    @Column(name = "DELIVERY_METHOD", nullable = false)
    private String deliveryMethod;
    
    @Column(name = "ENDPOINT_URL")
    private String endpointUrl;
    
    @Column(name = "AUTHORIZATION_HEADER", length = 4000)
    private String authorizationHeader;
    
    @Column(name = "EVENTS_REQUESTED", columnDefinition = "TEXT")
    private String eventsRequested;
    
    @Column(name = "ADDITIONAL_PARAMETERS", columnDefinition = "TEXT")
    private String additionalParameters;
}
