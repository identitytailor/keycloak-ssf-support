package com.identitytailor.keycloak.ssf.receiver.delivery.poll;

/**
 * Poll requests have three variations, see: https://www.rfc-editor.org/rfc/rfc8936.html#section-2.4
 */
public enum PollingMode {

    /**
     * In this scenario, a SET Recipient asks for the next set of events where no previous SET deliveries are acknowledged (such as in the initial poll request).
     */
    POLL_ONLY,

    /**
     * In this scenario, a SET Recipient sets the maxEvents value to 0 along with ack and setErrs members indicating the SET Recipient is acknowledging previously received SETs and does not want to receive any new SETs in response to the request.
     */
    ACK_ONLY,

    /**
     * In this scenario, a SET Recipient is both acknowledging previously received SETs using the ack and setErrs members and will wait for the next group of SETs in the SET Transmitters response.
     */
    POLL_AND_ACK
}
