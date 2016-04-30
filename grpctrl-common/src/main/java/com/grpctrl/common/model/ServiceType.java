package com.grpctrl.common.model;

/**
 * Defines the type of service implementations supported by this system.
 */
public enum ServiceType {
    /** An in-memory implementation used primarily for testing. */
    MEMORY,
    /** A JDBC implementation used for production operations. */
    DATABASE;
}
