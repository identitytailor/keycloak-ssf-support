package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import org.keycloak.models.RealmModel;

/**
 * RFC 8936 Poll-Based Security Event Token (SET) Delivery Using HTTP
 *
 * https://www.rfc-editor.org/rfc/rfc8936.html
 */
public interface SharedSignalsStreamPoller {

    void pollEvents(SecurityEventPollingContext pollingContext, SecurityEventPollingConfig config, RealmModel realm, ReceiverModel receiverModel);
}
