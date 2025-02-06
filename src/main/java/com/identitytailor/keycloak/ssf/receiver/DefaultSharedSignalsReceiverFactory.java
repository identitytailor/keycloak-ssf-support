package com.identitytailor.keycloak.ssf.receiver;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

@JBossLog
@AutoService(SharedSignalsReceiverFactory.class)
public class DefaultSharedSignalsReceiverFactory implements SharedSignalsReceiverFactory {

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String getHelpText() {
        return "Default Shared Signals Event Receiver";
    }

    @Override
    public DefaultSharedSignalsReceiver create(KeycloakSession session) {
        return new DefaultSharedSignalsReceiver(session);
    }

    @Override
    public DefaultSharedSignalsReceiver create(KeycloakSession session, ComponentModel model) {
        return new DefaultSharedSignalsReceiver(session, model);
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        // NOOP
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        log.infof("Created default shared signals receiver for realm '%s'", realm.getId());
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }
}
