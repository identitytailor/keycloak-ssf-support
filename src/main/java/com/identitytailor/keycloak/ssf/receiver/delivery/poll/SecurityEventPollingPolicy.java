package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import java.time.Duration;

public class SecurityEventPollingPolicy {

    protected PollingMode pollingMode;

    protected int maxEvents = 32;

    protected Duration pollingInterval = Duration.ofSeconds(5);

    public PollingMode getPollingMode() {
        return pollingMode;
    }

    public void setPollingMode(PollingMode pollingMode) {
        this.pollingMode = pollingMode;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Duration getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Duration pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
