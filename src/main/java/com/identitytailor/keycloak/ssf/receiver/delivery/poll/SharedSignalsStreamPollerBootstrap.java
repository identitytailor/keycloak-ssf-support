package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.identitytailor.keycloak.ssf.SharedSignalsProvider;
import com.identitytailor.keycloak.ssf.receiver.ReceiverModel;
import com.identitytailor.keycloak.ssf.receiver.SharedSignalsReceiver;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@JBossLog
public class SharedSignalsStreamPollerBootstrap {

    private final KeycloakSessionFactory keycloakSessionFactory;

    protected Duration pollingInterval;

    public SharedSignalsStreamPollerBootstrap(KeycloakSessionFactory keycloakSessionFactory, Duration pollingInterval) {
        this.keycloakSessionFactory = keycloakSessionFactory;
        this.pollingInterval = pollingInterval;
    }

    public void schedulePolling() {
        log.debugf("Start security event poller");

        // TODO revise receiver polling scheduling:
        /*
        0) Ensure polling is only run on one node in cluster (only on coordinator node ?)
        1) Find all realms with receivers
        2) Start a virtual thread per receiver
        3) receivers can sleep a configured timeout and schedule new virtual threads to reschedule their run

        Bookeeping about started receiver vthreads in "pollingContext".
        How to detect crashed receivers?
        How to detect hanging receivers?
        How to securely propagate a session in a vt context? (maybe use session factory and create a session for ever vt run)
         */

        ExecutorService executorService = createSharedSignalsStreamPollerExecutor();
        executorService.submit(() -> {

            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
                log.errorf(e, "Caught exception while processing polling in thread: %s", t.getName());
            });

            SecurityEventPollingContext pollingContext = new SecurityEventPollingContext();

            while (!Thread.currentThread().isInterrupted()) {

                KeycloakModelUtils.runJobInTransaction(keycloakSessionFactory, session -> {

                    SharedSignalsProvider sharedSignals = session.getProvider(SharedSignalsProvider.class);

                    Set<String> realmIdsWithReceivers = sharedSignals.getRealmIdsWithReceivers();
                    for (String realmId : realmIdsWithReceivers) {
                        RealmModel realm = session.realms().getRealm(realmId);
                        session.getContext().setRealm(realm);

                        pollEventsFromReceiversInRealm(session, realm, pollingContext);
                    }

                    log.tracef("Next poll in %s seconds", pollingInterval.toSeconds());
                });

                try {
                    Thread.sleep(pollingInterval);
                } catch (InterruptedException e) {
                    log.debugf(e, "Interrupted while waiting for polling security events.");
                }
            }
        });
    }

    protected void pollEventsFromReceiversInRealm(KeycloakSession session, RealmModel realm, SecurityEventPollingContext pollingContext) {

        List<ReceiverModel> receivers = realm.getComponentsStream(realm.getId(), SharedSignalsReceiver.class.getName()) //
                .map(ReceiverModel::new) //
                .filter(ReceiverModel::isPollDelivery) //
                .toList();

        if (receivers.isEmpty()) {
            return;
        }

        log.tracef("Found %d receivers in realm %s.", receivers.size(), realm.getName());

        for (ReceiverModel receiverModel : receivers) {
            SharedSignalsStreamPoller poller = new DefaultSharedSignalsStreamPoller(session);

            log.tracef("Polling security events from receiver. realm=%s receiver=%s", realm.getName(), receiverModel.getAlias());
            try {
                poller.pollEvents(pollingContext, realm, receiverModel);
            } catch (Exception e) {
                log.debugf(e, "Caught exception while polling events. realm=%s receiver=%s", realm.getName(), receiverModel.getAlias());
            }
        }
    }

    protected ExecutorService createSharedSignalsStreamPollerExecutor() {
        return Executors.newSingleThreadExecutor(r -> new Thread(r, "SharedSignalsStreamPoller"));
    }
}
