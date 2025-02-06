package com.identitytailor.keycloak.ssf.event.types.risc;

/**
 * Account Enabled signals that the account identified by the subject has been enabled.
 */
public class AccountEnabled extends RiscEvent {

    /**
     * See: https://openid.net/specs/openid-risc-profile-specification-1_0.html#rfc.section.2.4
     */
    public static final String TYPE = "https://schemas.openid.net/secevent/risc/event-type/account-enabled";

    public AccountEnabled() {
        super(TYPE);
    }
}
