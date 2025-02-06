package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

@JBossLog
public class SharedSignalsStreamPollerBootstrap {

    private final KeycloakSessionFactory keycloakSessionFactory;

    public SharedSignalsStreamPollerBootstrap(KeycloakSessionFactory keycloakSessionFactory) {
        this.keycloakSessionFactory = keycloakSessionFactory;
    }

    public void schedulePolling() {
        log.debugf("Start security event poller");

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {

            SecurityEventPollingPolicy pollingPolicy = new SecurityEventPollingPolicy();
            pollingPolicy.setPollingMode(PollingMode.POLL_AND_ACK);
            pollingPolicy.setMaxEvents(10);

            SecurityEventPollingContext pollingContext = new SecurityEventPollingContext();

            while (!Thread.currentThread().isInterrupted()) {

                Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
                    log.errorf(e, "Caught exception while processing polling...");
                });

                KeycloakModelUtils.runJobInTransaction(keycloakSessionFactory, session -> {

                    SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);

                    Set<String> realmIdsWithReceivers = sharedSignals.getRealmIdsWithReceivers();
                    for (String realmId : realmIdsWithReceivers) {
                        RealmModel realm = session.realms().getRealm(realmId);

                        session.getContext().setRealm(realm);

                        List<ReceiverModel> receivers = realm.getComponentsStream(realm.getId(), SharedSignalsReceiver.class.getName()) //
                                .map(ReceiverModel::new) //
                                .filter(ReceiverModel::isPollDelivery) //
                                .toList();

                        if (receivers.isEmpty()) {
                            continue;
                        }

                        log.tracef("Found %d receivers in realm %s.", receivers.size(), realm.getName());

                        for (ReceiverModel receiverModel : receivers) {
                            SharedSignalsStreamPoller poller = new DefaultSharedSignalsStreamPoller(session);

                            SecurityEventPollingConfig config = new SecurityEventPollingConfig();
                            config.setPollingPolicy(pollingPolicy);
                            config.setAcknowledgeImmediately(false);

                            log.tracef("Polling security events from receiver. realm=%s alias=%s", realm.getName(), receiverModel.getAlias());
                            poller.pollEvents(pollingContext, config, realm, receiverModel);
                        }
                    }

                    log.tracef("Next poll in %s seconds", pollingPolicy.getPollingInterval().toSeconds());
                });

                try {
                    Thread.sleep(pollingPolicy.getPollingInterval());
                } catch (InterruptedException e) {
                    log.debugf(e, "Interrupted while waiting for polling security events.");
                }
            }
        });
    }
}
