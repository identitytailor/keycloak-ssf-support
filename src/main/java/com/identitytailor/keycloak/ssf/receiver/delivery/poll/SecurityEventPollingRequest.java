package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

/**
 * See: https://www.rfc-editor.org/rfc/rfc8936.html#section-2.2
 */
public class SecurityEventPollingRequest {

    @JsonIgnore
    protected PollingMode pollingMode;

    /**
     * An OPTIONAL integer value indicating the maximum number of unacknowledged SETs to be returned. The SET Transmitter SHOULD NOT send more SETs than the specified maximum. If more than the maximum number of SETs are available, the SET Transmitter determines which to return first; the oldest SETs available MAY be returned first, or another selection algorithm MAY be used, such as prioritizing SETs in some manner that makes sense for the use case. A value of 0 MAY be used by SET Recipients that would like to perform an acknowledge-only request. This enables the Recipient to use separate HTTP requests for acknowledgement and reception of SETs. If this parameter is omitted, no limit is placed on the number of SETs to be returned.
     */
    @JsonProperty("maxEvents")
    protected Integer maxEvents;

    /**
     * An OPTIONAL JSON boolean value that indicates the SET Transmitter SHOULD return an immediate response even if no results are available (short polling). The default value is false, which indicates the request is to be treated as an HTTP long poll, per Section 2 of [RFC6202]. The timeout for the request is part of the configuration between the participants, which is out of scope of this specification.
     */
    @JsonProperty("returnImmediately")
    protected Boolean returnImmediately;

    /**
     * A JSON array of strings whose values are the jti [RFC7519] values of successfully received SETs that are being acknowledged. If there are no outstanding SETs to acknowledge, this member is omitted or contains an empty array. Once a SET has been acknowledged, the SET Transmitter is released from any obligation to retain the SET.
     */
    @JsonProperty("ack")
    protected Set<String> ack;

    /**
     * A JSON object with one or more members whose keys are the jti values of invalid SETs received. The values of these objects are themselves JSON objects that describe the errors detected using the err and description values specified in Section 2.6. If there are no outstanding SETs with errors to report, this member is omitted or contains an empty JSON object.
     */
    @JsonProperty("setErrs")
    protected Map<String, Object> setErrs;

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(Integer maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Boolean getReturnImmediately() {
        return returnImmediately;
    }

    public void setReturnImmediately(Boolean returnImmediately) {
        this.returnImmediately = returnImmediately;
    }

    public Set<String> getAck() {
        return ack;
    }

    public void setAck(Set<String> ack) {
        this.ack = ack;
    }

    public Map<String, Object> getSetErrs() {
        return setErrs;
    }

    public void setSetErrs(Map<String, Object> setErrs) {
        this.setErrs = setErrs;
    }

    public PollingMode getPollingMode() {
        return pollingMode;
    }

    public void setPollingMode(PollingMode pollingMode) {
        this.pollingMode = pollingMode;
    }

    @Override
    public String toString() {
        return "SecurityEventPollingRequest{" +
               "maxEvents=" + maxEvents +
               ", returnImmediately=" + returnImmediately +
               ", ack=" + ack +
               ", setErrs=" + setErrs +
               '}';
    }
}
