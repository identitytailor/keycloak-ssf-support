package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * See: https://www.rfc-editor.org/rfc/rfc8936.html#section-2.3
 */
public class SecurityEventPollingResponse {

    /**
     * A JSON object containing zero or more SETs being returned. Each member name is the jti of a SET to be delivered, and its value is a JSON string representing the corresponding SET. If there are no outstanding SETs to be transmitted, the JSON object SHALL be empty. Note that both SETs being transmitted for the first time and SETs that are being retransmitted after not having been acknowledged are communicated here.
     */
    @JsonProperty("sets")
    // @JsonDeserialize(using = SecurityEventPollingResponseDeserializer.class)
    protected Map<String, String> sets;

    /**
     * A JSON boolean value that indicates if more unacknowledged SETs are available to be returned. This member MAY be omitted, with the meaning being the same as including it with the boolean value false.
     */
    @JsonProperty("moreAvailable")
    protected Boolean moreAvailable;

    @JsonIgnore
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    public Map<String, String> getSets() {
        return sets;
    }

    public void setSets(Map<String, String> sets) {
        this.sets = sets;
    }

    public Boolean getMoreAvailable() {
        return moreAvailable;
    }

    public void setMoreAvailable(Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }

    public boolean isMoreAvailable() {
        return Boolean.TRUE.equals(moreAvailable);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @JsonAnySetter
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public String toString() {
        return "SecurityEventPollingResponse{" +
               "sets=" + sets +
               ", moreAvailable=" + moreAvailable +
               '}';
    }
}
