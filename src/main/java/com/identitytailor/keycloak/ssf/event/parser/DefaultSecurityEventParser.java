package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Token;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.KeycloakSession;

import java.nio.charset.StandardCharsets;

@JBossLog
public class DefaultSecurityEventParser implements SecurityEventParser {

    protected final KeycloakSession session;

    public DefaultSecurityEventParser(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public SecurityEventToken parseSecurityEventToken(String encodedSecurityEventToken, SharedSignalsReceiver receiver) {

        try {
            // custom decode method to use keys from ReceiverComponent
            // var securityEventToken_ = session.tokens().decode(encodedSecurityEventToken, SecurityEventToken.class);
            var securityEventToken = decode(encodedSecurityEventToken, SecurityEventToken.class, receiver);
            return securityEventToken;
        } catch (Exception e) {
            throw new SharedSignalsParsingException("Could not parse security event token", e);
        }
    }

    public <T extends Token> T decode(String token, Class<T> clazz, SharedSignalsReceiver receiver) {
        if (token == null) {
            return null;
        }

        try {
            JWSInput jws = new JWSInput(token);
            JWSHeader header = jws.getHeader();
            String kid = header.getKeyId();
            String alg = header.getRawAlgorithm();

            // TODO select key on algorithm
            KeyWrapper key = receiver.getKeys().filter(kw -> kw.getKid().equals(kid)).findFirst().orElse(null);
            if (key == null) {
                throw new SharedSignalsParsingException("Could not find key with kid " + kid);
            }

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, alg);
            if (signatureProvider == null) {
                throw new SharedSignalsParsingException("Could not find verifier for alg " + alg);
            }

            boolean valid = signatureProvider.verifier(key).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());
            return valid ? jws.readJsonContent(clazz) : null;
        } catch (Exception e) {
            log.trace("Failed to decode token", e);
            return null;
        }
    }
}
