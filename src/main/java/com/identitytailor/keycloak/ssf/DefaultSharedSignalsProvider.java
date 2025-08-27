package com.identitytailor.keycloak.ssf;

import com.google.auto.service.AutoService;
import com.identitytailor.keycloak.ssf.event.SecurityEventToken;
import com.identitytailor.keycloak.ssf.event.listener.DefaultSecurityEventListener;
import com.identitytailor.keycloak.ssf.event.listener.SecurityEventListener;
import com.identitytailor.keycloak.ssf.event.parser.DefaultSecurityEventParser;
import com.identitytailor.keycloak.ssf.event.parser.SecurityEventParser;
import com.identitytailor.keycloak.ssf.event.processor.DefaultSecurityEventProcessor;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessor;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.DefaultSharedSignalsStreamPoller;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.SharedSignalsStreamPoller;
import com.identitytailor.keycloak.ssf.receiver.delivery.push.PushEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManagementEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManager;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverStreamManager;
import com.identitytailor.keycloak.ssf.receiver.streamclient.DefaultSharedSignalsStreamClient;
import com.identitytailor.keycloak.ssf.receiver.streamclient.SharedSignalsStreamClient;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.DefaultTransmitterClient;
import com.identitytailor.keycloak.ssf.receiver.transmitterclient.TransmitterClient;
import com.identitytailor.keycloak.ssf.receiver.verification.DefaultSecurityEventsVerificationClient;
import com.identitytailor.keycloak.ssf.receiver.verification.SecurityEventsVerificationClient;
import com.identitytailor.keycloak.ssf.storage.DefaultSharedSignalsStorage;
import com.identitytailor.keycloak.ssf.storage.VerificationStore;
import com.identitytailor.keycloak.ssf.streams.model.DeliveryMethod;
import com.identitytailor.keycloak.ssf.transmitter.delivery.SecurityEventTokenDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenMapper;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterConfigurationEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterService;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfEventStore;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.storage.memory.InMemoryEventStore;
import com.identitytailor.keycloak.ssf.transmitter.storage.memory.InMemoryStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamManagementEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamService;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatusEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.verification.VerificationEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.verification.VerificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.HashSet;
import java.util.Set;

public class DefaultSharedSignalsProvider implements SharedSignalsProvider {

    protected final KeycloakSession session;

    protected SecurityEventParser securityEventParser;

    protected SecurityEventProcessor securityEventProcessor;

    protected SecurityEventListener securityEventListener;

    protected PushEndpoint pushEndpoint;

    protected PollEndpoint pollEndpoint;

    protected StreamManagementEndpoint streamManagementEndpoint;

    protected StreamStatusEndpoint streamStatusEndpoint;

    protected TransmitterConfigurationEndpoint transmitterConfigurationEndpoint;

    protected VerificationEndpoint verificationEndpoint;

    protected ReceiverManagementEndpoint receiverManagementEndpoint;

    protected SharedSignalsStreamPoller sharedSignalsStreamPoller;

    protected SecurityEventsVerificationClient securityEventsVerifier;

    protected VerificationStore verificationStore;

    protected SharedSignalsStreamClient streamClient;

    protected TransmitterClient transmitterClient;

    protected ReceiverManager receiverManager;

    protected ReceiverStreamManager receiverStreamManager;

    protected PollDeliveryService pollDeliveryService;

    protected PushDeliveryService pushDeliveryService;

    protected StreamService streamService;

    protected SsfEventStore eventStore;

    protected SsfStreamStore streamStore;;

    protected TransmitterService transmitterService;

    protected VerificationService verificationService;

    protected SecurityEventTokenEncoder securityEventTokenEncoder;

    protected SecurityEventTokenDeliveryService securityEventTokenDeliveryService;

    public DefaultSharedSignalsProvider(KeycloakSession session) {
        this.session = session;
    }

    protected SecurityEventParser getSecurityEventParser() {
        if (securityEventParser == null) {
            securityEventParser = new DefaultSecurityEventParser(session);
        }
        return securityEventParser;
    }

    protected SecurityEventProcessor getSecurityEventProcessor() {
        if (securityEventProcessor == null) {
            securityEventProcessor = new DefaultSecurityEventProcessor(
                    this,
                    getSecurityEventListener(),
                    getSharedSignalsStore()
            );
        }
        return securityEventProcessor;
    }

    protected PushEndpoint getPushEndpoint() {
        if (pushEndpoint == null) {
            pushEndpoint = new PushEndpoint();
        }
        return pushEndpoint;
    }

    protected ReceiverManagementEndpoint getReceiverManagementEndpoint() {
        if (receiverManagementEndpoint == null) {
            receiverManagementEndpoint = new ReceiverManagementEndpoint(session, getReceiverManager());
        }
        return receiverManagementEndpoint;
    }

    protected ReceiverManager getReceiverManager() {
        if (receiverManager == null) {
            receiverManager = new ReceiverManager(session);
        }
        return receiverManager;
    }

    protected SecurityEventListener getSecurityEventListener() {
        if (securityEventListener == null) {
            securityEventListener = new DefaultSecurityEventListener(session);
        }
        return securityEventListener;
    }

    protected SharedSignalsStreamPoller getPoller() {
        if (sharedSignalsStreamPoller == null) {
            sharedSignalsStreamPoller = new DefaultSharedSignalsStreamPoller(session);
        }
        return sharedSignalsStreamPoller;
    }

    protected SecurityEventsVerificationClient getSecurityEventsVerifier() {
        if (securityEventsVerifier == null) {
            securityEventsVerifier = new DefaultSecurityEventsVerificationClient(session);
        }
        return securityEventsVerifier;
    }

    protected VerificationStore getSharedSignalsStore() {
        if (verificationStore == null) {
            verificationStore = new DefaultSharedSignalsStorage(session);
        }
        return verificationStore;
    }

    protected SharedSignalsStreamClient getStreamClient() {
        if (streamClient == null) {
            streamClient = new DefaultSharedSignalsStreamClient(session);
        }
        return streamClient;
    }

    protected TransmitterClient getTransmitterClient() {
        if (transmitterClient == null) {
            transmitterClient = new DefaultTransmitterClient(session);
        }
        return transmitterClient;
    }

    @Override
    public SecurityEventToken parseSecurityEventToken(String encodedSecurityEventToken, SecurityEventProcessingContext processingContext) {
        var parser = getSecurityEventParser();
        return parser.parseSecurityEventToken(encodedSecurityEventToken, processingContext.getReceiver());
    }

    @Override
    public void processSecurityEvents(SecurityEventProcessingContext securityEventProcessingContext) {
        var processor = getSecurityEventProcessor();
        processor.processSecurityEvents(securityEventProcessingContext);
    }

    @Override
    public VerificationStore verificationStore() {
        return getSharedSignalsStore();
    }

    @Override
    public PushEndpoint pushEndpoint() {
        return getPushEndpoint();
    }

    @Override
    public ReceiverManagementEndpoint receiverManagementEndpoint() {
        return getReceiverManagementEndpoint();
    }

    @Override
    public ReceiverStreamManager receiverStreamManager() {
        return getReceiverStreamManager();
    }

    protected ReceiverStreamManager getReceiverStreamManager() {
        if (receiverStreamManager == null) {
            receiverStreamManager = new ReceiverStreamManager(this);
        }
        return receiverStreamManager;
    }

    @Override
    public SharedSignalsStreamPoller securityEventPoller() {
        return getPoller();
    }

    @Override
    public SecurityEventsVerificationClient verificationClient() {
        return getSecurityEventsVerifier();
    }

    @Override
    public SharedSignalsStreamClient streamClient() {
        return getStreamClient();
    }

    @Override
    public TransmitterClient transmitterClient() {
        return getTransmitterClient();
    }

    @Override
    public Set<String> getRealmIdsWithReceivers() {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        Query query = em.createQuery("select c.realm.id from ComponentEntity c " +
                                     "inner join ComponentConfigEntity cc " +
                                     "on c = cc.component and cc.name = :configName and cc.value = :configValue " +
                                     "where c.providerType = :providerType")
                .setParameter("configName", "deliveryMethod")
                .setParameter("configValue", DeliveryMethod.POLL.name())
                .setParameter("providerType", SharedSignalsReceiver.class.getName());

        Set<String> realmIds = new HashSet<>(query.getResultList());
        return realmIds;
    }

    @Override
    public SecurityEventProcessingContext createSecurityEventProcessingContext(SecurityEventToken securityEventToken, String receiverAlias) {
        SecurityEventProcessingContext context = new SecurityEventProcessingContext();
        context.setSecurityEventToken(securityEventToken);
        context.setSession(session);
        SharedSignalsReceiver receiver = getReceiverManager().lookupReceiver(session.getContext(), receiverAlias);
        context.setReceiver(receiver);
        return context;
    }

    @Override
    public PollEndpoint pollEndpoint() {
        return getPollEndpoint();
    }

    protected PollEndpoint getPollEndpoint() {
        if (pollEndpoint == null) {
            pollEndpoint = new PollEndpoint(session, pollDeliveryService());
        }
        return pollEndpoint;
    }

    @Override
    public StreamManagementEndpoint streamManagementEndpoint() {
        return getStreamManagementEndpoint();
    }

    protected StreamManagementEndpoint getStreamManagementEndpoint() {

        if (streamManagementEndpoint == null) {
            streamManagementEndpoint = new StreamManagementEndpoint(streamService());
        }
        return streamManagementEndpoint;
    }

    @Override
    public StreamStatusEndpoint streamStatusEndpoint() {
        return getStreamStatusEndpoint();
    }

    protected StreamStatusEndpoint getStreamStatusEndpoint() {

        if (streamStatusEndpoint == null) {
            streamStatusEndpoint = new StreamStatusEndpoint(streamService());
        }
        return streamStatusEndpoint;
    }

    @Override
    public TransmitterConfigurationEndpoint transmitterConfigurationEndpoint() {
        return getTransmitterConfigurationEndpoint();
    }

    protected TransmitterConfigurationEndpoint getTransmitterConfigurationEndpoint() {
        if (transmitterConfigurationEndpoint == null) {
            transmitterConfigurationEndpoint = new TransmitterConfigurationEndpoint(session, transmitterService());
        }
        return transmitterConfigurationEndpoint;
    }

    @Override
    public VerificationEndpoint verificationEndpoint() {
        return getVerificationEndpoint();
    }

    protected VerificationEndpoint getVerificationEndpoint() {
        if (verificationEndpoint == null) {
            verificationEndpoint = new VerificationEndpoint(verificationService());
        }
        return verificationEndpoint;
    }

    @Override
    public VerificationService verificationService() {
        return getVerificationService();
    }

    @Override
    public SsfEventStore eventStore() {
        return getEventStore();
    }

    protected SsfEventStore getEventStore() {
        if (eventStore == null) {
            eventStore = new InMemoryEventStore(session);
        }
        return eventStore;
    }

    @Override
    public SsfStreamStore streamStore() {
        return getStreamStore();
    }

    protected SsfStreamStore getStreamStore() {
        if (streamStore == null) {
            streamStore = new InMemoryStreamStore();
        }
        return streamStore;
    }

    protected VerificationService getVerificationService() {
        if (verificationService == null) {
            verificationService = new VerificationService(streamStore(), new SecurityEventTokenMapper(transmitterService()), securityEventTokenDeliveryService());
        }
        return verificationService;
    }

    @Override
    public TransmitterService transmitterService() {
        return getTransmitterService();
    }

    protected TransmitterService getTransmitterService() {
        if (transmitterService == null) {
            transmitterService = new TransmitterService(session);
        }
        return transmitterService;
    }

    @Override
    public PollDeliveryService pollDeliveryService() {
        return getPollDeliveryService();
    }

    protected PollDeliveryService getPollDeliveryService() {
        if (pollDeliveryService == null) {
            pollDeliveryService = new PollDeliveryService(session, new InMemoryEventStore(session));
        }
        return pollDeliveryService;
    }

    public PushDeliveryService pushDeliveryService() {
        return getPushDeliveryService();
    }

    protected PushDeliveryService getPushDeliveryService() {
        if (pushDeliveryService == null) {
            pushDeliveryService = new PushDeliveryService(session);
        }
        return pushDeliveryService;
    }

    @Override
    public StreamService streamService() {
        return getStreamService();
    }

    protected StreamService getStreamService() {
        if (streamService == null) {
            streamService = new StreamService(session, streamStore(), transmitterService());
        }
        return streamService;
    }

    @Override
    public ReceiverManager receiverManager() {
        return getReceiverManager();
    }

    @Override
    public SecurityEventTokenEncoder securityEventTokenEncoder() {
        return getSecurityEventTokenEncoder();
    }

    @Override
    public SecurityEventTokenDeliveryService securityEventTokenDeliveryService() {
        return getSecurityEventTokenDeliveryService();
    }

    protected SecurityEventTokenDeliveryService getSecurityEventTokenDeliveryService() {
        if (securityEventTokenDeliveryService == null) {
            securityEventTokenDeliveryService = new SecurityEventTokenDeliveryService(streamStore(), securityEventTokenEncoder(), pushDeliveryService(), pollDeliveryService());
        }
        return securityEventTokenDeliveryService;
    }

    protected SecurityEventTokenEncoder getSecurityEventTokenEncoder() {
        if (securityEventTokenEncoder == null) {
            securityEventTokenEncoder = new SecurityEventTokenEncoder(session);
        }
        return securityEventTokenEncoder;
    }

    @AutoService(SharedSignalsProviderFactory.class)
    public static class Factory implements SharedSignalsProviderFactory {

        @Override
        public String getId() {
            return "default";
        }

        @Override
        public SharedSignalsProvider create(KeycloakSession keycloakSession) {
            return new DefaultSharedSignalsProvider(keycloakSession);
        }

        @Override
        public void init(Config.Scope scope) {
        }

        @Override
        public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

        }

        @Override
        public void close() {

        }
    }
}
