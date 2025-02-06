package com.identitytailor.keycloak.ssf;

import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManagementEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverStreamManager;
import com.identitytailor.keycloak.ssf.receiver.streamclient.SharedSignalsStreamClient;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.receiver.verification.SecurityEventsVerificationClient;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.SharedSignalsStreamPoller;
import com.identitytailor.keycloak.ssf.receiver.delivery.push.SharedSignalsPushEndpoint;
import com.identitytailor.keycloak.ssf.storage.SharedSignalsStore;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.provider.Provider;

import java.util.Set;
import java.util.stream.Stream;

public interface SharedSignalsProvider extends Provider {

    @Override
    default void close() {
        // NOOP
    }

    SecurityEventToken parse(String encodedSecurityEventToken);

    void processSecurityEvents(SecurityEventProcessingContext securityEventProcessingContext);

    SecurityEventProcessingContext createSecurityEventProcessingContext(SecurityEventToken securityEventToken, String receiverAlias);

    SharedSignalsPushEndpoint pushEndpoint();

    ReceiverManagementEndpoint receiverManagementEndpoint();

    ReceiverStreamManager receiverStreamManager();

    SharedSignalsStreamPoller securityEventPoller();

    SecurityEventsVerificationClient verificationClient();

    SharedSignalsStore storage();

    SharedSignalsStreamClient streamClient();

    TransmitterClient transmitterClient();

    Set<String> getRealmIdsWithReceivers();

}
