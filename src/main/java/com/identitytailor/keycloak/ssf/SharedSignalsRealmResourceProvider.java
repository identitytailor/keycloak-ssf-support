package com.identitytailor.keycloak.ssf;

import com.google.auto.service.AutoService;
import com.identitytailor.keycloak.ssf.receiver.delivery.poll.SharedSignalsStreamPollerBootstrap;
import com.identitytailor.keycloak.ssf.receiver.delivery.push.PushEndpoint;
import com.identitytailor.keycloak.ssf.receiver.management.ReceiverManagementEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.metadata.TransmitterConfigurationEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamManagementEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamStatusEndpoint;
import com.identitytailor.keycloak.ssf.transmitter.verification.VerificationEndpoint;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.utils.KeycloakSessionUtil;

import java.time.Duration;

@JBossLog
public class SharedSignalsRealmResourceProvider implements RealmResourceProvider {

    @Override
    public Object getResource() {
        return this;
    }

    // Transmitter Endpoints below
    @Path("/.well-known/ssf-configuration")
    public TransmitterConfigurationEndpoint getTransmitterConfigurationEndpoint() {
        return SharedSignalsProvider.current().transmitterConfigurationEndpoint();
    }

    @Path("/streams/poll")
    public PollEndpoint getPollEndpoint() {
        authenticate();
        return SharedSignalsProvider.current().pollEndpoint();
    }

    @Path("/streams")
    public StreamManagementEndpoint getStreamManagementEndpoint() {
        authenticate();
        return SharedSignalsProvider.current().streamManagementEndpoint();
    }

    @Path("/streams/status")
    public StreamStatusEndpoint getStreamStatusEndpoint() {
        authenticate();
        return SharedSignalsProvider.current().streamStatusEndpoint();
    }

    @Path("/verify")
    public VerificationEndpoint getVerificationEndpoint() {
        authenticate();
        return SharedSignalsProvider.current().verificationEndpoint();
    }

    protected AuthenticationManager.AuthResult authenticate() {
        var session = KeycloakSessionUtil.getKeycloakSession();
        var authenticator = new AppAuthManager.BearerTokenAuthenticator(session);
        var auth = authenticator.authenticate();
        if (auth == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return auth;
    }

    // Receiver Endpoints below

    /**
     * $ISSUER/ssf/push/caepdev
     *
     * For example: https://tdworkshops.ngrok.dev/auth/realms/ssf-demo/ssf/push/caepdev
     * @return
     */
    @Path("/push")
    public PushEndpoint pushEndpoint() {
        authenticate();
        return SharedSignalsProvider.current().pushEndpoint();
    }

    // Receiver Management Endpoints below

    /**
     * $ISSUER/ssf/management
     *
     * For example: https://tdworkshops.ngrok.dev/auth/realms/ssf-demo/ssf/management
     * @return
     */
    @Path("/management")
    public ReceiverManagementEndpoint receiverManagementEndpoint() {
        // TODO check manage permissions
        authenticate();
        return SharedSignalsProvider.current().receiverManagementEndpoint();
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
            // TODO fetch pollingInterval from config
            Duration pollingInterval = Duration.ofSeconds(5);
            new SharedSignalsStreamPollerBootstrap(keycloakSessionFactory, pollingInterval).schedulePolling();
        }
    }
}
