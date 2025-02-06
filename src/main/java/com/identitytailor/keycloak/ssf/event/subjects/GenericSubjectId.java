package com.identitytailor.keycloak.ssf.event.subjects;

public class GenericSubjectId extends SubjectId {

    public GenericSubjectId() {
        super(null);
    }

    @Override
    public String toString() {
        return "GenericSubjectId{" +
               "format='" + format + '\'' +
               ", attributes=" + attributes +
               '}';
    }
}
