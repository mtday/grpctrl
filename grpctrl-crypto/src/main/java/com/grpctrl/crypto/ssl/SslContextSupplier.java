package com.grpctrl.crypto.ssl;

import com.google.common.base.Charsets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.EncryptionException;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.crypto.store.TrustStoreSupplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to an {@link SSLContext} implementation.
 */
@Provider
public class SslContextSupplier implements Supplier<SSLContext>, Factory<SSLContext>, ContextResolver<SSLContext> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nonnull
    private final KeyStoreSupplier keyStoreSupplier;

    @Nonnull
    private final TrustStoreSupplier trustStoreSupplier;

    @Nonnull
    private final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier;

    @Nullable
    private volatile SSLContext singleton = null;

    /**
     * @param configSupplier provides access to the static system configuration properties
     * @param keyStoreSupplier provides access to the system key store
     * @param trustStoreSupplier provides access to the system trust store
     * @param passwordBasedEncryptionSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public SslContextSupplier(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final KeyStoreSupplier keyStoreSupplier,
            @Nonnull final TrustStoreSupplier trustStoreSupplier,
            @Nonnull final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.keyStoreSupplier = Objects.requireNonNull(keyStoreSupplier);
        this.trustStoreSupplier = Objects.requireNonNull(trustStoreSupplier);
        this.passwordBasedEncryptionSupplier = Objects.requireNonNull(passwordBasedEncryptionSupplier);
    }

    /**
     * @return the provider of the static system configuration properties
     */
    @Nonnull
    protected ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    /**
     * @return provides access to the system key store
     */
    @Nonnull
    protected KeyStoreSupplier getKeyStoreSupplier() {
        return this.keyStoreSupplier;
    }

    /**
     * @return provides access to the system trust store
     */
    @Nonnull
    protected TrustStoreSupplier getTrustStoreSupplier() {
        return this.trustStoreSupplier;
    }

    /**
     * @return provides support for password-based encryption and decryption
     */
    @Nonnull
    protected PasswordBasedEncryptionSupplier getPasswordBasedEncryptionSupplier() {
        return this.passwordBasedEncryptionSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public SSLContext get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (SslContextSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public SSLContext getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public SSLContext provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final SSLContext sslContext) {
        // Nothing to do.
    }

    @Nonnull
    private SSLContext create() {
        try {
            if (!getConfigSupplier().get().getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
                return SSLContext.getDefault();
            }

            final KeyStore keyStore = getKeyStoreSupplier().get();
            final KeyStore trustStore = getTrustStoreSupplier().get();

            final String encryptedPassword =
                    getConfigSupplier().get().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_PASSWORD.getKey());
            final char[] decryptedPassword =
                    getPasswordBasedEncryptionSupplier().get().decryptProperty(encryptedPassword, Charsets.UTF_8)
                            .toCharArray();

            final KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, decryptedPassword);

            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext;
        } catch (final EncryptionException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to create SSLContext", exception);
        }
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SslContextSupplier.class).to(SslContextSupplier.class).in(Singleton.class);
            bindFactory(SslContextSupplier.class).to(SSLContext.class).in(Singleton.class);
        }
    }
}
