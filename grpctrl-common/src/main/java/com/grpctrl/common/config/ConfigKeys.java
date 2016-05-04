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

    /** The host on which the system API should run (and bind to on start-up). */
    SYSTEM_API_HOST,
    /** The port on which the system API should run. */
    SYSTEM_API_PORT,
    /** The host on which the system administration web app should run (and bind to on start-up). */
    SYSTEM_WEB_HOST,
    /** The port on which the system administration web app should run. */
    SYSTEM_WEB_PORT,

    /** The environment variable defining the shared secret value to use for password-based encryption. */
    CRYPTO_SHARED_SECRET_VARIABLE,
    /** Whether the web server is running with SSL enabled. */
    CRYPTO_SSL_ENABLED,
    /** The key store file used in the SSL configuration. */
    CRYPTO_SSL_KEYSTORE_FILE,
    /** The type of the key store file, typically either JKS or PKCS12. */
    CRYPTO_SSL_KEYSTORE_TYPE,
    /** The password to use when reading the key store file. */
    CRYPTO_SSL_KEYSTORE_PASSWORD,
    /** The trust store file used in the SSL configuration. */
    CRYPTO_SSL_TRUSTSTORE_FILE,
    /** The type of the trust store file, typically either JKS or PKCS12. */
    CRYPTO_SSL_TRUSTSTORE_TYPE,
    /** The password to use when reading the trust store file. */
    CRYPTO_SSL_TRUSTSTORE_PASSWORD,

    /** The JDBC database connection URL. */
    DB_URL,
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
    /** Whether the database schema should be dropped during initialization (for testing). */
    DB_CLEAN,
    /** Whether the migration sql scripts should be applied to the database. */
    DB_MIGRATE,

    /** The timeout to wait for the remote server to connect. */
    CLIENT_TIMEOUT_CONNECT,
    /** The timeout to wait for the remote server to respond to a read. */
    CLIENT_TIMEOUT_READ,
    /** The timeout to wait for the remote server to respond to a write. */
    CLIENT_TIMEOUT_WRITE,
    /** Whether the client should attempt to retry on failure. */
    CLIENT_FAILURE_RETRY;

    /**
     * @return the key to use when retrieving the common configuration value from the system configuration file
     */
    @Nonnull
    public String getKey() {
        return name().toLowerCase(Locale.ENGLISH).replaceAll("_", ".");
    }
}
