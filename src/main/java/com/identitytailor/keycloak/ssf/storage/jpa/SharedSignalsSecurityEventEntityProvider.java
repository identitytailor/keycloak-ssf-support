package com.identitytailor.keycloak.ssf.storage.jpa;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.List;

public class SharedSignalsSecurityEventEntityProvider implements JpaEntityProvider {

    public static final String PROVIDER_ID = "shared-signals-jpa-entities";

    @Override
    public List<Class<?>> getEntities() {
        return List.of(SecurityEventEntity.class);
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/shared-signals-changelog-1.0.0.xml";
    }

    @Override
    public String getFactoryId() {
        return PROVIDER_ID;
    }

    @Override
    public void close() {
        // NOOP
    }

    @AutoService(JpaEntityProviderFactory.class)
    public static class Factory implements JpaEntityProviderFactory {

        @Override
        public String getId() {
            return PROVIDER_ID;
        }

        @Override
        public JpaEntityProvider create(KeycloakSession session) {
            return new SharedSignalsSecurityEventEntityProvider();
        }

        @Override
        public void init(Config.Scope config) {
            // NOOP
        }

        @Override
        public void postInit(KeycloakSessionFactory factory) {
            // NOOP
        }

        @Override
        public void close() {
            // NOOP
        }
    }
}
