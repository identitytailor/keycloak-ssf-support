package com.identitytailor.keycloak.ssf.event.parser;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.keys.TransmitterKeyManager;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsHacks;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Token;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.ServerAsymmetricSignatureVerifierContext;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.nio.charset.StandardCharsets;

@JBossLog
public class DefaultSecurityEventParser implements SecurityEventParser {

    public static final String SECURITY_EVENT_PARSER_SESSION_ATTRIBUTE = "securityEventProcessingContext";

    protected final KeycloakSession session;

    protected final SharedSignalsProvider sharedSignals;

    public DefaultSecurityEventParser(KeycloakSession session, SharedSignalsProvider sharedSignals) {
        this.session = session;
        this.sharedSignals = sharedSignals;
    }

    @Override
    public SecurityEventToken parse(String encodedSecurityEventToken) {

        try {
            // custom decode method to use keys from ReceiverComponent
            //var securityEventToken = session.tokens().decode(encodedSecurityEventToken, SecurityEventToken.class);
            var securityEventToken = decode(encodedSecurityEventToken, SecurityEventToken.class);
//            JWSInput jws = new JWSInput(encodedSecurityEventToken);
//            securityEventToken = jws.readJsonContent(SecurityEventToken.class);
            return securityEventToken;
        } catch (Exception e) {
            throw new SharedSignalsParsingException("Could not parse security event token", e);
        }
    }

    public <T extends Token> T decode(String token, Class<T> clazz) {
        if (token == null) {
            return null;
        }

        try {
            JWSInput jws = new JWSInput(token);

            RealmModel realm = session.getContext().getRealm();
            ReceiverModel receiverModel = (ReceiverModel)session.getAttribute(SharedSignalsHacks.RECEIVER_MODEL_SESSION_ATTRIBUTE);

            JWSHeader header = jws.getHeader();
            String kid = header.getKeyId();

            KeyWrapper key = TransmitterKeyManager.getPublicKeyWrapper(realm, receiverModel, kid);

            if (key == null) {
                throw new SharedSignalsParsingException("Cloud not find key with kid " + kid);
            }
            boolean valid = new ServerAsymmetricSignatureVerifierContext(key).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());

            return valid ? jws.readJsonContent(clazz) : null;
        } catch (Exception e) {
            log.trace("Failed to decode token", e);
            return null;
        }
    }
}
