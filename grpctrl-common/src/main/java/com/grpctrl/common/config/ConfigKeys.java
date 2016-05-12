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

    /** The host on which the system should run (and bind to on start-up). */
    SYSTEM_HOST,
    /** The port on which the system should run. */
    SYSTEM_PORT,

    /** The base URL of this system, something like:  https://myhost/. */
    SYSTEM_BASE_URL,

    /** The API key to use when communicating with the OAuth2 authorization service. */
    AUTH_API_KEY,
    /** The API secret to use when communicating with the OAuth2 authorization service. */
    AUTH_API_SECRET,
    /** The scope(s) of the data from the auth API we want to be authorized to access. */
    AUTH_API_SCOPE,
    /** The callback URL to which the user will be forwarded after authentication. */
    AUTH_API_CALLBACK,
    /** The resources URL from which this service will retrieve user account information. */
    AUTH_API_RESOURCE_USER,
    /** The resources URL from which this service will retrieve user email information. */
    AUTH_API_RESOURCE_EMAIL,
    /** The accept header value to send to the resource URL. */
    AUTH_API_ACCEPT,

    /** The directory containing the web resource base (content files) */
    WEB_CONTENT,

    /** The environment variable defining the shared secret value to use for password-based encryption. */
    CRYPTO_SHARED_SECRET_VARIABLE,
    /** The default shared secret value used for password-based encryption. */
    CRYPTO_SHARED_SECRET_DEFAULT,
    /** Whether the web server is running with SSL enabled. */
    CRYPTO_SSL_ENABLED,
    /** The key store file used in the SSL configuration. */
    CRYPTO_SSL_KEYSTORE_FILE,
    /** The type of the key store file, typically either JKS or PKCS12. */
    CRYPTO_SSL_KEYSTORE_TYPE,
    /** The password to use when reading the key store file. */
    CRYPTO_SSL_KEYSTORE_PASSWORD,
    /** The trust store file used in the SSL configuration. */

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
    CLIENT_FAILURE_RETRY,

    /** The number of threads in the thread pool used to perform security lookup operations */
    SECURITY_THREADS;

    /**
     * @return the key to use when retrieving the common configuration value from the system configuration file
     */
    @Nonnull
    public String getKey() {
        return name().toLowerCase(Locale.ENGLISH).replaceAll("_", ".");
    }
}
