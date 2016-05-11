package com.grpctrl.common.model;

import java.security.Principal;

/**
 * Describes the types of roles available to a user account.
 */
public enum UserRole implements Principal {
    /** An administrator user account */
    ADMIN,

    /** A typical user account */
    USER;

    @Override
    public String getName() {
        return name();
    }
}
