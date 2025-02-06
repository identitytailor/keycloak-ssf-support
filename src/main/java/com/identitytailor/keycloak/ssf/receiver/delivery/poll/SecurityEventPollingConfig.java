package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

public class SecurityEventPollingConfig {

    protected String streamId;

    protected String pollUri;

    protected SecurityEventPollingPolicy pollingPolicy;

    protected String authToken;

    protected boolean acknowledgeImmediately;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getPollUri() {
        return pollUri;
    }

    public void setPollUri(String pollUri) {
        this.pollUri = pollUri;
    }

    public SecurityEventPollingPolicy getPollingPolicy() {
        return pollingPolicy;
    }

    public void setPollingPolicy(SecurityEventPollingPolicy pollingPolicy) {
        this.pollingPolicy = pollingPolicy;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isAcknowledgeImmediately() {
        return acknowledgeImmediately;
    }

    public void setAcknowledgeImmediately(boolean acknowledgeImmediately) {
        this.acknowledgeImmediately = acknowledgeImmediately;
    }

    @Override
    public String toString() {
        return "SecurityEventPollingConfig{" +
               "streamId='" + streamId + '\'' +
               ", pollUri='" + pollUri + '\'' +
               ", pollingPolicy=" + pollingPolicy +
               ", authToken='****'" +
               ", acknowledgeImmediately=" + acknowledgeImmediately +
               '}';
    }
}
