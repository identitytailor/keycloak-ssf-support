package com.identitytailor.keycloak.ssf;

import com.google.auto.service.AutoService;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.SharedSignalsStreamPollerBootstrap;
import com.identitytailor.keycloak.ssf.receiver.delivery.push.SharedSignalsPushEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManagementEndpoint;
import jakarta.ws.rs.Path;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

import static org.keycloak.utils.KeycloakSessionUtil.getKeycloakSession;

@JBossLog
public class SharedSignalsRealmResourceProvider implements RealmResourceProvider {

    @Override
    public Object getResource() {
        return this;
    }

    /**
     * $ISSUER/ssf/push/caepdev
     *
     * For example: https://tdworkshops.ngrok.dev/auth/realms/ssf-demo/ssf/push/caepdev
     * @return
     */
    @Path("/push")
    public SharedSignalsPushEndpoint pushEndpoint() {
        return getKeycloakSession().getProvider(SharedSignalsProvider.class).pushEndpoint();
    }

    /**
     * $ISSUER/ssf/management
     *
     * For example: https://tdworkshops.ngrok.dev/auth/realms/ssf-demo/ssf/management
     * @return
     */
    @Path("/management")
    public ReceiverManagementEndpoint receiverManagementEndpoint() {
        // TODO check manage permissions
        return getKeycloakSession().getProvider(SharedSignalsProvider.class).receiverManagementEndpoint();
    }


    @Override
    public void close() {
        // NOOP
    }

    @AutoService(RealmResourceProviderFactory.class)
    public static class Factory implements RealmResourceProviderFactory {

        private static final SharedSignalsRealmResourceProvider INSTANCE = new SharedSignalsRealmResourceProvider();

        /**
         * Exposes the SSF endpoints via $ISSUER/ssf
         * @return
         */
        @Override
        public String getId() {
            return "ssf";
        }

        @Override
        public RealmResourceProvider create(KeycloakSession keycloakSession) {
            return INSTANCE;
        }

        @Override
        public void init(Config.Scope scope) {
        }

        @Override
        public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
            keycloakSessionFactory.register(event -> {
                if (event instanceof PostMigrationEvent) {
                    bootstrapPolling(keycloakSessionFactory);
                }
            });
        }

        @Override
        public void close() {
        }

        protected void bootstrapPolling(KeycloakSessionFactory keycloakSessionFactory) {
            new SharedSignalsStreamPollerBootstrap(keycloakSessionFactory).schedulePolling();
        }
    }
}
