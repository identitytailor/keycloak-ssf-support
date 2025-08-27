package com.identitytailor.keycloak.ssf.transmitter.delivery;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.delivery.polling.PollDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;
import java.util.Set;

@JBossLog
public class SecurityEventTokenDeliveryService {

    private final SsfStreamStore streamStore;
    private final SecurityEventTokenEncoder securityEventTokenEncoder;
    private final PushDeliveryService pushDeliveryService;
    private final PollDeliveryService pollDeliveryService;

    public SecurityEventTokenDeliveryService(SsfStreamStore streamStore,
                                             SecurityEventTokenEncoder securityEventTokenEncoder,
                                             PushDeliveryService pushDeliveryService,
                                             PollDeliveryService pollDeliveryService) {
        this.streamStore = streamStore;
        this.securityEventTokenEncoder = securityEventTokenEncoder;
        this.pushDeliveryService = pushDeliveryService;
        this.pollDeliveryService = pollDeliveryService;
    }

    /**
     * Gets the event type from a security event token.
     *
             * @param event The security event token
     * @return The event type, or null if not found
     */
    protected String getEventType(SecurityEventToken event) {
        if (event.getEvents() != null && !event.getEvents().isEmpty()) {
            return event.getEvents().keySet().iterator().next();
        }
        return null;
    }

    /**
     * Delivers an event to all applicable streams.
     *
     * @param event The event to deliver
     */
    public void deliverEvent(SecurityEventToken event) {
        try {
            List<StreamConfiguration> streams = streamStore.getAllStreams();

            for (StreamConfiguration stream : streams) {
                // Skip disabled or paused streams
                if (shouldSkipStream(stream)) {
                    continue;
                }

                // Check if the stream is interested in this event type
                String eventType = getEventType(event);
                if (eventType != null && stream.getEventsRequested() != null &&
                    !stream.getEventsRequested().contains(eventType)) {
                    continue;
                }

                // Deliver the event based on the stream's delivery method
                var delivery = stream.getDelivery();
                String deliveryMethod = delivery.getMethod();

                if ("urn:ietf:rfc:8935".equals(deliveryMethod)) {
                    // PUSH delivery
                    try {
                        String encodedEvent = securityEventTokenEncoder.encode(event);
                        pushDeliveryService.deliverEvent(stream, encodedEvent);
                    } catch (Exception e) {
                        log.error("Error delivering event via PUSH to stream " + stream.getStreamId(), e);
                    }
                } else if ("urn:ietf:rfc:8936".equals(deliveryMethod)) {
                    // POLL delivery
                    try {
                        pollDeliveryService.storeEvent(event);
                    } catch (Exception e) {
                        log.error("Error storing event for POLL delivery for stream " + stream.getStreamId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error delivering event", e);
        }
    }

    protected boolean shouldSkipStream(StreamConfiguration stream) {
        return Set.of("paused", "disabled").contains(stream.getStatus());
    }
}
