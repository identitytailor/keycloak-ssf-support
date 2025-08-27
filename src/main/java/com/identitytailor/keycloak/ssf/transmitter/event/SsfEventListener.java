package com.identitytailor.keycloak.ssf.transmitter.event;

import com.google.auto.service.AutoService;
import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.delivery.SecurityEventTokenDeliveryService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Keycloak event listener that maps Keycloak events to SSF events.
 */
@JBossLog
public class SsfEventListener implements EventListenerProvider {

    private final SecurityEventTokenMapper securityEventTokenMapper;
    private final SecurityEventTokenDeliveryService securityEventTokenDeliveryService;

    public SsfEventListener(SecurityEventTokenMapper securityEventTokenMapper,
                            SecurityEventTokenDeliveryService securityEventTokenDeliveryService) {
        this.securityEventTokenMapper = securityEventTokenMapper;
        this.securityEventTokenDeliveryService = securityEventTokenDeliveryService;
    }

    @Override
    public void onEvent(Event event) {

        SecurityEventToken securityEventToken = securityEventTokenMapper.toSecurityEvent(event);
        if (securityEventToken == null) {
            return;
        }

        try {
            securityEventTokenDeliveryService.deliverEvent(securityEventToken);
        } catch (Exception e) {
            log.warn("Failed to deliver SSF Security Event", e);
        }
    }


    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Process admin events if needed
    }

    @Override
    public void close() {
        // No resources to close
    }

    @AutoService(EventListenerProviderFactory.class)
    public static class Factory implements EventListenerProviderFactory {

        private static final String ID = "ssf-events";

        @Override
        public EventListenerProvider create(KeycloakSession session) {

            SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);

            // Create event generator
            SecurityEventTokenMapper securityEventTokenMapper = new SecurityEventTokenMapper(sharedSignals.transmitterService());

            // Create and return the event mapper
            return new SsfEventListener(securityEventTokenMapper, sharedSignals.securityEventTokenDeliveryService());
        }


        @Override
        public void init(Config.Scope config) {
            // No initialization needed
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
            // NOOP
        }

        @Override
        public void close() {
            // No resources to close
        }

        @Override
        public String getId() {
            return ID;
        }
    }

}
