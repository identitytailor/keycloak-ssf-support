package com.identitytailor.keycloak.ssf.transmitter.event;

import com.identitytailor.keycloak.ssf.Ssf;
import org.keycloak.Token;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;

public class SecurityEventTokenEncoder {

    private final KeycloakSession session;

    public SecurityEventTokenEncoder(KeycloakSession session) {
        this.session = session;
    }

    public String encode(Token token) {

        // TODO configure signature algorithm for SET tokens
        String signatureAlgorithm = session.tokens().signatureAlgorithm(token.getCategory());

        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, signatureAlgorithm);
        SignatureSignerContext signer = signatureProvider.signer();

        String encodedToken = newJwsBuilder().jsonContent(token).sign(signer);
        return encodedToken;
    }

    protected JWSBuilder newJwsBuilder() {
        return new JWSBuilder().type(Ssf.SECEVENT_JWT_TYPE);
    }
}
