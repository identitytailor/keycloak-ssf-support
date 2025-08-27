package com.identitytailor.keycloak.ssf.transmitter.streams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

/**
 * Represents a stream configuration in the SSF transmitter.
 *
 * See: https://openid.net/specs/openid-sharedsignals-framework-1_0-05.html
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamConfiguration {

    /**
     * Transmitter-Supplied, REQUIRED. A string that uniquely identifies the stream.
     * A Transmitter MUST generate a unique ID for each of its non-deleted streams at the time of stream creation.
     * Transmitters SHOULD use character set described in Section 2.3 of [RFC3986] to generate the stream ID
     */
    @JsonProperty("stream_id")
    private String streamId;

    /**
     * Transmitter-Supplied, REQUIRED. A URL using the https scheme with no query or fragment component that the Transmitter asserts as its Issuer Identifier.
     * This MUST be identical to the "iss" Claim value in Security Event Tokens issued from this Transmitter.
     */
    @JsonProperty("iss")
    private String issuer;

    /**
     * Transmitter-Supplied, REQUIRED. A string or an array of strings containing an audience claim as defined in JSON Web Token (JWT)[RFC7519] that identifies the Event Receiver(s) for the Event Stream.
     * This property cannot be updated. If multiple Receivers are specified then the Transmitter SHOULD know that these Receivers are the same entity.
     */
    @JsonProperty("aud")
    private Set<String> audience;

    /**
     * Transmitter-Supplied, OPTIONAL. An array of URIs identifying the set of events supported by the Transmitter for this Receiver.
     * If omitted, Event Transmitters SHOULD make this set available to the Event Receiver via some other means (e.g. publishing it in online documentation).
     */
    @JsonProperty("events_supported")
    private Set<String> eventsSupported;

    /**
     * Receiver-Supplied, OPTIONAL. An array of URIs identifying the set of events that the Receiver requested.
     * A Receiver SHOULD request only the events that it understands and it can act on.
     * This is configurable by the Receiver. A Transmitter MUST ignore any array values that it does not understand.
     * This array SHOULD NOT be empty.
     */
    @JsonProperty("events_requested")
    private Set<String> eventsRequested;

    /**
     * Transmitter-Supplied, REQUIRED. An array of URIs identifying the set of events that the Transmitter MUST include in the stream.
     * This is a subset (not necessarily a proper subset) of the intersection of "events_supported" and "events_requested".
     * A Receiver MUST rely on the values received in this field to understand which event types it can expect from the Transmitter.
     */
    @JsonProperty("events_delivered")
    private Set<String> eventsDelivered;

    /**
     * REQUIRED. A JSON object containing a set of name/value pairs specifying configuration parameters for the SET delivery method.
     * The actual delivery method is identified by the special key "method" with the value being a URI as defined in Section 6.1.
     */
    @JsonProperty("delivery")
    private StreamDeliveryConfiguration delivery;

    /**
     * Transmitter-Supplied, OPTIONAL. An integer indicating the minimum amount of time in seconds that must pass in between verification requests.
     * If an Event Receiver submits verification requests more frequently than this, the Event Transmitter MAY respond with a 429 status code.
     * An Event Transmitter SHOULD NOT respond with a 429 status code if an Event Receiver is not exceeding this frequency.
     */
    @JsonProperty("min_verification_interval")
    private Integer minVerificationInterval;

    /**
     * Receiver-Supplied, OPTIONAL. A string that describes the properties of the stream.
     * This is useful in multi-stream systems to identify the stream for human actors.
     * The transmitter MAY truncate the string beyond an allowed max length.
     */
    @JsonProperty("description")
    private String description;

    /**
     * Transmitter-Supplied, OPTIONAL. The refreshable inactivity timeout of the stream in seconds.
     * After the timeout duration passes with no eligible activity from the Receiver, as defined below, the Transmitter MAY either pause, disable, or delete the stream.
     * The syntax is the same as that of expires_in from Section A.14 of [RFC6749].
     */
    @JsonProperty("inactivity_timeout")
    private Integer inactivityTimeout;

    @JsonIgnore
    private String status;

    @JsonIgnore
    private String statusReason;

    @JsonIgnore
    private Integer createdAt;

    @JsonIgnore
    private Integer updatedAt;
}
