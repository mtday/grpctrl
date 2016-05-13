package com.grpctrl.crypto.ssl;

import com.google.common.base.Charsets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.InternalServerErrorException;
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
    private final PasswordBasedEncryptionSupplier pbeSupplier;

    @Nullable
    private volatile SSLContext singleton = null;

    /**
     * @param configSupplier provides access to the static system configuration properties
     * @param keyStoreSupplier provides access to the system key store
     * @param pbeSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public SslContextSupplier(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final KeyStoreSupplier keyStoreSupplier,
            @Nonnull final PasswordBasedEncryptionSupplier pbeSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.keyStoreSupplier = Objects.requireNonNull(keyStoreSupplier);
        this.pbeSupplier = Objects.requireNonNull(pbeSupplier);
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
            if (!this.configSupplier.get().getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
                return SSLContext.getDefault();
            }

            final KeyStore keyStore = this.keyStoreSupplier.get();

            final String encryptedPassword =
                    this.configSupplier.get().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_PASSWORD.getKey());
            final char[] decryptedPassword =
                    this.pbeSupplier.get().decryptProperty(encryptedPassword, Charsets.UTF_8).toCharArray();

            final KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, decryptedPassword);

            // Trusting everybody.
            final TrustManager[] trustManagers = {new TrustEverybody()};

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());
            return sslContext;
        } catch (final UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                KeyManagementException exception) {
            throw new InternalServerErrorException("Failed to create SSLContext", exception);
        }
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SslContextSupplier.class).to(SslContextSupplier.class).in(Singleton.class);
        }
    }

    private static class TrustEverybody implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
