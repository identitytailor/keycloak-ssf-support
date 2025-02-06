package com.identitytailor.keycloak.ssf.keys;

import com.identitytailor.keycloak.ssf.event.parser.SharedSignalsParsingException;
import com.identitytailor.keycloak.ssf.receiver.ReceiverKeyModel;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManager;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.RealmModel;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TransmitterKeyManager {

    public static PublicKey decodePublicKey(String key){
        try{
            byte[] byteKey = Base64.getDecoder().decode(key);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            throw new SharedSignalsParsingException("Could not decode public key", e);
        }
    }

    public static KeyWrapper getPublicKeyWrapper(RealmModel realm, ReceiverModel receiverModel, String kid) {

        ReceiverKeyModel receiverKeyModel = new ReceiverKeyModel(realm.getComponent(ReceiverManager.createReceiverKeyComponentId(receiverModel, kid)));

        String encodedPublicKey = receiverKeyModel.getPublicKey();
        PublicKey publicKey = decodePublicKey(encodedPublicKey);

        KeyWrapper key = new KeyWrapper();
        key.setKid(kid);
        key.setAlgorithm(receiverKeyModel.getAlgorithm());
        key.setUse(receiverKeyModel.getKeyUse());
        key.setPublicKey(publicKey);

        return key;
    }
}
