package com.grpctrl.common.model;

/**
 * Defines the type of a member within a group.
 */
public enum MemberType {
    /** A single unique individual. */
    INDIVIDUAL,
    /** A group of individuals, representing a child group (a group within a group). */
    GROUP;
}
