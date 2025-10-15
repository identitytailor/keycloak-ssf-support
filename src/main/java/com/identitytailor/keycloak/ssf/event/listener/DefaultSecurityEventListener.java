package com.identitytailor.keycloak.ssf.event.listener;

import com.identitytailor.keycloak.ssf.event.processor.SecurityEventProcessingContext;
import com.identitytailor.keycloak.ssf.event.subjects.SubjectId;
import com.identitytailor.keycloak.ssf.event.subjects.SubjectUserLookup;
import com.identitytailor.keycloak.ssf.event.types.SecurityEvent;
import com.identitytailor.keycloak.ssf.event.types.caep.SessionRevoked;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;

import java.util.List;

@JBossLog
public class DefaultSecurityEventListener implements SecurityEventListener {

    protected final KeycloakSession session;

    public DefaultSecurityEventListener(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onSecurityEvent(SecurityEventProcessingContext processingContext, String securityEventId, SecurityEvent securityEvent) {
        String eventType = securityEvent.getEventType();
        SubjectId subjectId = securityEvent.getSubjectId();
        var eventClass = securityEvent.getClass();
        log.infof("Security event received. jti=%s eventType=%s subjectId=%s eventClass=%s", securityEventId, eventType, subjectId, eventClass);

        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        switch (securityEvent) {
            case SessionRevoked sessionRevoked -> {

                UserModel user = lookupUser(realm, subjectId);
                if (user != null) {
                    List<UserSessionModel> sessions = session.sessions().getUserSessionsStream(realm, user).toList();
                    if (!sessions.isEmpty()) {
                        for (var userSession : sessions) {
                            session.sessions().removeUserSession(realm, userSession);
                        }
                        log.debugf("Removed %s sessions for user. realm=%s userId=%s", sessions.size(), realm.getName(), user.getId());
                    }

                }
            }
            default -> {
            }
        }
    }

    protected UserModel lookupUser(RealmModel realm, SubjectId subjectId) {
        return SubjectUserLookup.lookupUser(session, realm, subjectId);
    }

}
