package com.grpctrl.common.config;

import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Defines the static configuration keys expected to exist in the system configuration.
 */
public enum ConfigKeys {
    /** The name of the system. */
    SYSTEM_NAME,
    /** The current running version of the system. */
    SYSTEM_VERSION,

    /** Whether the web server is running with SSL enabled. */
    SSL_ENABLED,
    /** The key store file used in the SSL configuration. */
    SSL_KEYSTORE_FILE,
    /** The type of the key store file, typically either JKS or PKCS12. */
    SSL_KEYSTORE_TYPE,
    /** The password to use when reading the key store file. */
    SSL_KEYSTORE_PASSWORD,
    /** The trust store file used in the SSL configuration. */
    SSL_TRUSTSTORE_FILE,
    /** The type of the trust store file, typically either JKS or PKCS12. */
    SSL_TRUSTSTORE_TYPE,
    /** The password to use when reading the trust store file. */
    SSL_TRUSTSTORE_PASSWORD,

    /** The JDBC DataSource class implementation. */
    DB_DATASOURCE_CLASS,
    /** The user name to use when connecting to the database. */
    DB_USERNAME,
    /** The password to use when connecting to the database. */
    DB_PASSWORD,
    /** The minimum number of database connections to keep idle. */
    DB_MINIMUM_IDLE,
    /** The maximum number of connections to keep in the pool. */
    DB_MAXIMUM_POOL_SIZE,
    /** The amount of time a connection is allowed to sit idle in the pool. */
    DB_TIMEOUT_IDLE,
    /** The amount of time we are willing to wait for an available connection before an exception is thrown. */
    DB_TIMEOUT_CONNECTION,

    /** The underlying service implementation type. */
    SERVICE_IMPL;

    /**
     * @return the key to use when retrieving the common configuration value from the system configuration file
     */
    @Nonnull
    public String getKey() {
        return name().toLowerCase(Locale.ENGLISH).replaceAll("_", ".");
    }
}
