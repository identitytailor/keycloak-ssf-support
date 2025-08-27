package com.identitytailor.keycloak.ssf.transmitter.verification;

import com.identitytailor.keycloak.ssf.transmitter.SecurityEventToken;
import com.identitytailor.keycloak.ssf.transmitter.delivery.SecurityEventTokenDeliveryService;
import com.identitytailor.keycloak.ssf.transmitter.streams.StreamConfiguration;
import com.identitytailor.keycloak.ssf.transmitter.storage.SsfStreamStore;
import com.identitytailor.keycloak.ssf.transmitter.event.SecurityEventTokenMapper;
import lombok.extern.jbosslog.JBossLog;

/**
 * Service for handling SSF stream verification.
 */
@JBossLog
public class VerificationService {

    private final SsfStreamStore streamStore;
    private final SecurityEventTokenMapper securityEventTokenMapper;
    private final SecurityEventTokenDeliveryService securityEventTokenDeliveryService;

    public VerificationService(SsfStreamStore streamStore,
                               SecurityEventTokenMapper securityEventTokenMapper,
                               SecurityEventTokenDeliveryService securityEventTokenDeliveryService) {
        this.streamStore = streamStore;
        this.securityEventTokenMapper = securityEventTokenMapper;
        this.securityEventTokenDeliveryService = securityEventTokenDeliveryService;
    }

    /**
     * Triggers a verification event for a stream.
     *
     * @param verificationRequest The verification request
     * @return true if the verification was triggered, false if the stream was not found
     */
    public boolean triggerVerification(VerificationRequest verificationRequest) {
        String streamId = verificationRequest.getStreamId();
        StreamConfiguration stream = streamStore.getStream(streamId);
        
        if (stream == null) {
            log.warnf("Stream not found for verification. streamId=%s", streamId);
            return false;
        }
        
        // Generate a verification event
        SecurityEventToken verificationEventToken = securityEventTokenMapper.generateVerificationEvent(stream, verificationRequest.getState());
        securityEventTokenDeliveryService.deliverEvent(verificationEventToken);
        
        return true;
    }
}
