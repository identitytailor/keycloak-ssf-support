package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import lombok.extern.jbosslog.JBossLog;
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
            var securityEventToken = decode(encodedSecurityEventToken, receiver);
            return securityEventToken;
        } catch (Exception e) {
            throw new SharedSignalsParsingException("Could not parse security event token", e);
        }
    }

    protected SecurityEventToken decode(String encodedSecurityEventToken, SharedSignalsReceiver receiver) {

        if (encodedSecurityEventToken == null) {
            return null;
        }

        try {
            JWSInput jws = new JWSInput(encodedSecurityEventToken);
            JWSHeader header = jws.getHeader();
            String kid = header.getKeyId();
            String alg = header.getRawAlgorithm();

            KeyWrapper key = receiver.getKeys()
                    .filter(kw -> kw.getKid().equals(kid) && kw.getAlgorithm().equals(alg))
                    .findFirst()
                    .orElse(null);
            if (key == null) {
                throw new SharedSignalsParsingException("Could not find key with kid " + kid);
            }

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, alg);
            if (signatureProvider == null) {
                throw new SharedSignalsParsingException("Could not find verifier for alg " + alg);
            }

            byte[] tokenBytes = jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8);
            boolean valid = signatureProvider.verifier(key)
                    .verify(tokenBytes, jws.getSignature());
            return valid ? jws.readJsonContent(SecurityEventToken.class) : null;
        } catch (Exception e) {
            log.debug("Failed to decode token", e);
            return null;
        }
    }
}
