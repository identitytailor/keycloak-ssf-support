package com.identitytailor.keycloak.ssf.keys;

import com.identitytailor.keycloak.ssf.transmitter.SharedSignalsTransmitterMetadata;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;
import org.keycloak.util.JWKSUtils;

@JBossLog
public class TransmitterPublicKeyLoader implements PublicKeyLoader {

    protected final KeycloakSession session;

    protected final SharedSignalsTransmitterMetadata transmitterMetadata;

    public TransmitterPublicKeyLoader(KeycloakSession session, SharedSignalsTransmitterMetadata transmitterMetadata) {
        this.session = session;
        this.transmitterMetadata = transmitterMetadata;
    }

    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, transmitterMetadata.getJwksUri());
        return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
    }
}
