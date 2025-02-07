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
import com.identitytailor.keycloak.ssf.receiver.delivery.push.SharedSignalsPushEndpoint;
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
import com.identitytailor.keycloak.ssf.storage.SharedSignalsStore;
import com.identitytailor.keycloak.ssf.streams.model.DeliveryMethod;
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

    protected SharedSignalsPushEndpoint sharedSignalsPushEndpoint;

    protected ReceiverManagementEndpoint receiverManagementEndpoint;

    protected SharedSignalsStreamPoller sharedSignalsStreamPoller;

    protected SecurityEventsVerificationClient securityEventsVerifier;

    protected SharedSignalsStore sharedSignalsStore;

    protected SharedSignalsStreamClient streamClient;

    protected TransmitterClient transmitterClient;

    protected ReceiverManager receiverManager;

    protected ReceiverStreamManager receiverStreamManager;

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

    protected SharedSignalsPushEndpoint getPushEndpoint() {
        if (sharedSignalsPushEndpoint == null) {
            sharedSignalsPushEndpoint = new SharedSignalsPushEndpoint();
        }
        return sharedSignalsPushEndpoint;
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

    protected SharedSignalsStore getSharedSignalsStore() {
        if (sharedSignalsStore == null) {
            sharedSignalsStore = new DefaultSharedSignalsStorage(session);
        }
        return sharedSignalsStore;
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
    public SharedSignalsStore storage() {
        return getSharedSignalsStore();
    }

    @Override
    public SharedSignalsPushEndpoint pushEndpoint() {
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

        Set<String> realmIds = new HashSet<>();
        realmIds.addAll(query.getResultList());
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
