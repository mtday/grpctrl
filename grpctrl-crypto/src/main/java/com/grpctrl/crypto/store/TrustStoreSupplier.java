package com.grpctrl.crypto.store;

import com.google.common.base.Charsets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.typesafe.config.Config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.security.KeyStore;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link KeyStore} implementation.
 */
@Provider
public class TrustStoreSupplier extends StoreSupplier {
    /**
     * @param config provides access to the static system configuration properties
     * @param passwordBasedEncryptionSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public TrustStoreSupplier(
            @Nonnull final Config config,
            @Nonnull final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier) {
        super(config, passwordBasedEncryptionSupplier);
    }

    @Override
    @Nonnull
    public String getFile() {
        return getConfig().getString(ConfigKeys.CRYPTO_SSL_TRUSTSTORE_FILE.getKey());
    }

    @Override
    @Nonnull
    public String getType() {
        return getConfig().getString(ConfigKeys.CRYPTO_SSL_TRUSTSTORE_TYPE.getKey());
    }

    @Override
    @Nonnull
    public char[] getPassword() {
        final String encryptedPassword = getConfig().getString(ConfigKeys.CRYPTO_SSL_TRUSTSTORE_PASSWORD.getKey());
        return getPasswordBasedEncryptionSupplier().get().decryptProperty(encryptedPassword, Charsets.UTF_8)
                .toCharArray();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(TrustStoreSupplier.class).to(TrustStoreSupplier.class).in(Singleton.class);
            bindFactory(TrustStoreSupplier.class).to(KeyStore.class).named("trust-store").in(Singleton.class);
        }
    }
}
