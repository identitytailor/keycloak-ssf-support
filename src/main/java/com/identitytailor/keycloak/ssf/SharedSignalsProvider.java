package com.identitytailor.keycloak.ssf;

import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.SharedSignalsStreamPoller;
import com.identitytailor.keycloak.ssf.receiver.delivery.push.PushEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManagementEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManager;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverStreamManager;
import com.identitytailor.keycloak.ssf.receiver.streamclient.SharedSignalsStreamClient;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.receiver.verification.SecurityEventsVerificationClient;
import com.identitytailor.keycloak.ssf.storage.VerificationStore;
import com.identitytailor.keycloak.ssf.transmitter.delivery.SecurityEventTokenDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamManagementEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatusEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterConfigurationEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.verification.VerificationEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamService;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterService;
import com.identitytailor.keycloak.ssf.transmitter.verification.VerificationService;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import org.keycloak.provider.Provider;

import java.util.Set;

import static org.keycloak.utils.KeycloakSessionUtil.getKeycloakSession;

public interface SharedSignalsProvider extends Provider {

    @Override
    default void close() {
        // NOOP
    }

    SecurityEventToken parseSecurityEventToken(String encodedSecurityEventToken, SecurityEventProcessingContext processingContext);

    void processSecurityEvents(SecurityEventProcessingContext securityEventProcessingContext);

    SecurityEventProcessingContext createSecurityEventProcessingContext(SecurityEventToken securityEventToken, String receiverAlias);

    // SSF Transmitter Support
    PollEndpoint pollEndpoint();

    StreamManagementEndpoint streamManagementEndpoint();

    StreamStatusEndpoint streamStatusEndpoint();

    TransmitterConfigurationEndpoint transmitterConfigurationEndpoint();

    VerificationEndpoint verificationEndpoint();

    // SSF Receiver Support

    PushEndpoint pushEndpoint();

    ReceiverManagementEndpoint receiverManagementEndpoint();

    ReceiverStreamManager receiverStreamManager();

    SharedSignalsStreamPoller securityEventPoller();

    SecurityEventsVerificationClient verificationClient();

    VerificationStore verificationStore();

    SharedSignalsStreamClient streamClient();

    TransmitterClient transmitterClient();

    Set<String> getRealmIdsWithReceivers();

    PollDeliveryService pollDeliveryService();

    StreamService streamService();

    TransmitterService transmitterService();

    VerificationService verificationService();

    SsfEventStore eventStore();

    SsfStreamStore streamStore();

    PushDeliveryService pushDeliveryService();

    ReceiverManager receiverManager();

    SecurityEventTokenEncoder securityEventTokenEncoder();

    SecurityEventTokenDeliveryService securityEventTokenDeliveryService();

    static SharedSignalsProvider current() {
        return getKeycloakSession().getProvider(SharedSignalsProvider.class);
    }
}
