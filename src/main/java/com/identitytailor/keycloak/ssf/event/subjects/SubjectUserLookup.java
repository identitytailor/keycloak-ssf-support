package com.identitytailor.keycloak.ssf.event.subjects;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class SubjectUserLookup {

    public static UserModel lookupUser(KeycloakSession session, RealmModel realm, SubjectId subjectId) {

        return switch (subjectId) {
            case EmailSubjectId emailSubjectId -> getUserByEmail(session, realm, emailSubjectId.getEmail());
            case OpaqueSubjectId opaqueSubjectId -> getUserById(session, realm, opaqueSubjectId.getId());
            case IssuerSubjectId issuerSubjectId -> getUserByIssuerSub(session, realm, issuerSubjectId.getIss(), issuerSubjectId.getSub());
            case null, default -> {
                log.warnf("Lookup failed for unknown subject id. subjectId=%s", subjectId);
                yield null;
            }
        };
    }

    private static UserModel getUserByIssuerSub(KeycloakSession session, RealmModel realm, String iss, String sub) {

        String realmIssuer = "http://localhost:18080/auth/realms/ssf-demo";
                // TODO fixme cannot create current realmIssuer in async call context
                // Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName());
        if (realmIssuer.equals(iss)) {
            return getUserById(session, realm, sub);
        }

        // TODO lookup user by identity provider links
        return null;
    }

    private static UserModel getUserById(KeycloakSession session, RealmModel realm, String userId) {
        return session.users().getUserById(realm, userId);
    }

    private static UserModel getUserByEmail(KeycloakSession session, RealmModel realm, String email) {
        return session.users().getUserByEmail(realm, email);
    }
}
