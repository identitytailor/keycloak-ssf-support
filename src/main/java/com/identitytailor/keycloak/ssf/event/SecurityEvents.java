package com.identitytailor.keycloak.ssf.event;

import com.identitytailor.keycloak.ssf.event.types.GenericSecurityEvent;
import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;
import com.identitytailor.keycloak.ssf.event.types.VerificationEvent;
import com.identitytailor.keycloak.ssf.event.types.caep.AssuranceLevelChange;
import com.identitytailor.keycloak.ssf.event.types.caep.CaepEvent;
import com.identitytailor.keycloak.ssf.event.types.caep.CredentialChange;
import com.identitytailor.keycloak.ssf.event.types.caep.DeviceComplianceChange;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionEstablished;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionPresented;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionRevoked;
import com.identitytailor.keycloak.ssf.event.types.caep.TokenClaimsChanged;
import com.identitytailor.keycloak.ssf.event.types.risc.AccountCredentialChangeRequired;
import com.identitytailor.keycloak.ssf.event.types.risc.AccountDisabled;
import com.identitytailor.keycloak.ssf.event.types.risc.AccountEnabled;
import com.identitytailor.keycloak.ssf.event.types.risc.AccountPurged;
import com.identitytailor.keycloak.ssf.event.types.risc.CredentialCompromise;
import com.identitytailor.keycloak.ssf.event.types.risc.IdentifierChanged;
import com.identitytailor.keycloak.ssf.event.types.risc.IdentifierRecycled;
import com.identitytailor.keycloak.ssf.event.types.risc.OptIn;
import com.identitytailor.keycloak.ssf.event.types.risc.OptOutCancelled;
import com.identitytailor.keycloak.ssf.event.types.risc.OptOutEffective;
import com.identitytailor.keycloak.ssf.event.types.risc.OptOutInitiated;
import com.identitytailor.keycloak.ssf.event.types.risc.RecoveryActivated;
import com.identitytailor.keycloak.ssf.event.types.risc.RecoveryInformationChanged;
import com.identitytailor.keycloak.ssf.event.types.risc.RiscEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityEvents {

    public final static Map<String, Class<? extends CaepEvent>> CAEP_EVENTS_TYPES;
    public final static Map<String, Class<? extends RiscEvent>> RISC_EVENTS_TYPES;

    static {
        var caepEventTypes = new HashMap<String, Class<? extends CaepEvent>>();
        List.of( //
                new AssuranceLevelChange(), //
                new CredentialChange(), //
                new DeviceComplianceChange(), //
                new SessionEstablished(), //
                new SessionPresented(), //
                new SessionRevoked(), //
                new TokenClaimsChanged() //
        ).forEach(caepEvent -> caepEventTypes.put(caepEvent.getEventType(), caepEvent.getClass()));
        CAEP_EVENTS_TYPES = Collections.unmodifiableMap(caepEventTypes);

        var riscEventTypes = new HashMap<String, Class<? extends RiscEvent>>();
        List.of( //
                new AccountCredentialChangeRequired(), //
                new AccountDisabled(), //
                new AccountEnabled(), //
                new AccountPurged(), //
                new CredentialCompromise(), //
                new IdentifierChanged(), //
                new IdentifierRecycled(), //
                new OptIn(), //
                new OptOutInitiated(), //
                new OptOutCancelled(), //
                new OptOutEffective(), //
                new RecoveryActivated(), //
                new RecoveryInformationChanged() //
        ).forEach(riscEvent -> riscEventTypes.put(riscEvent.getEventType(), riscEvent.getClass()));
        RISC_EVENTS_TYPES =  Collections.unmodifiableMap(riscEventTypes);
    }

    public static boolean isCaepEvent(SecurityEvent rawSecurityEvent) {
        return CAEP_EVENTS_TYPES.containsKey(rawSecurityEvent.getEventType());
    }

    public static boolean isRiscEvent(SecurityEvent rawSecurityEvent) {
        return RISC_EVENTS_TYPES.containsKey(rawSecurityEvent.getEventType());
    }

    public static boolean isVerificationEventType(String eventType) {
        return VerificationEvent.TYPE.equals(eventType);
    }

    public static Class<? extends SecurityEvent> getSecurityEventType(String eventType) {

        if (isVerificationEventType(eventType)) {
            return VerificationEvent.class;
        }

        var caepEventType = CAEP_EVENTS_TYPES.get(eventType);
        if (caepEventType != null) {
            return caepEventType;
        }

        var riscEventType = RISC_EVENTS_TYPES.get(eventType);
        if (riscEventType != null) {
            return riscEventType;
        }

        return GenericSecurityEvent.class;
    }
}
