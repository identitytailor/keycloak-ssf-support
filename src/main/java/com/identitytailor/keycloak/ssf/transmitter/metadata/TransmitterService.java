package com.identitytailor.keycloak.ssf.transmitter.metadata;

import com.identitytailor.keycloak.ssf.event.types.caep.CredentialChange;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionRevoked;
import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for managing the SSF transmitter functionality.
 */
@JBossLog
public class TransmitterService {

    private final KeycloakSession session;

    public TransmitterService(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Returns the SSF transmitter configuration metadata.
     *
     * @return The SSF transmitter configuration metadata
     */
    public SharedSignalsTransmitterMetadata getTransmitterMetadata() {
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();
        String baseUrl = context.getUri().getBaseUri().toString() + "realms/" + realm.getName();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return createTransmitterMetadata(realm, baseUrl);
    }

    protected SharedSignalsTransmitterMetadata createTransmitterMetadata(RealmModel realm, String baseUrl) {

        SharedSignalsTransmitterMetadata metadata = new SharedSignalsTransmitterMetadata();
        metadata.setSpecVersion("1_0");
        metadata.setIssuer(baseUrl);
        metadata.setJwksUri(baseUrl + "/protocol/openid-connect/certs");
        metadata.setDeliveryMethodSupported(Set.of( //
                "urn:ietf:rfc:8935" // PUSH
//                "urn:ietf:rfc:8936"  // POLL
        ));

        // Set endpoints
        String ssfBasePath = baseUrl + "/ssf";
        metadata.setConfigurationEndpoint(ssfBasePath + "/streams");
        metadata.setStatusEndpoint(ssfBasePath + "/streams/status");
        metadata.setVerificationEndpoint(ssfBasePath + "/verify");

        // Set authorization schemes
        Map<String, String> oauthScheme = new HashMap<>();
        oauthScheme.put("spec_urn", "urn:ietf:rfc:6749");
        metadata.setAuthorizationSchemes(Collections.singletonList(oauthScheme));

        return metadata;
    }

    public Set<String> getSupportedEvents() {
        // TODO compute supported events for current realm
        return Set.of(CredentialChange.TYPE, SessionRevoked.TYPE);
    }
}
