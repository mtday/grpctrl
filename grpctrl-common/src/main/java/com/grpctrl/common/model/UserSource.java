package com.grpctrl.common.model;

/**
 * Describes the source of the user account.
 */
public enum UserSource {
    /** The user account is local in this system database */
    LOCAL,

    /** The user logged in via OAuth and github */
    GITHUB
}
