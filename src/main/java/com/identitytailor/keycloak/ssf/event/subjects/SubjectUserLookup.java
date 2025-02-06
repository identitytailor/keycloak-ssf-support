package com.identitytailor.keycloak.ssf.event.subjects;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class SubjectUserLookup {

    public static UserModel lookupUser(KeycloakSession session, RealmModel realm, SubjectId subjectId) {

        return switch (subjectId) {
            case EmailSubjectId emailSubjectId -> session.users().getUserByEmail(realm, emailSubjectId.getEmail());
            case OpaqueSubjectId opaqueSubjectId -> session.users().getUserById(realm, opaqueSubjectId.getId());
            case IssuerSubjectId issuerSubjectId ->
                // TODO lookup user by identity provider links
                    null;
            case null, default -> null;
        };

    }
}
