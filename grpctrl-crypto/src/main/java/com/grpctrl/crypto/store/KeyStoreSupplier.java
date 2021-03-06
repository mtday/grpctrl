package com.grpctrl.crypto.store;

import com.google.common.base.Charsets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;

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
public class KeyStoreSupplier extends StoreSupplier {
    /**
     * @param configSupplier provides access to the static system configuration properties
     * @param passwordBasedEncryptionSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public KeyStoreSupplier(
            @Nonnull final ConfigSupplier configSupplier,
            @Nonnull final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier) {
        super(configSupplier, passwordBasedEncryptionSupplier);
    }

    @Override
    @Nonnull
    public String getFile() {
        return getConfigSupplier().get().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_FILE.getKey());
    }

    @Override
    @Nonnull
    public String getType() {
        return getConfigSupplier().get().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_TYPE.getKey());
    }

    @Override
    @Nonnull
    public char[] getPassword() {
        final String encryptedPassword =
                getConfigSupplier().get().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_PASSWORD.getKey());
        return getPasswordBasedEncryptionSupplier().get().decryptProperty(encryptedPassword, Charsets.UTF_8)
                .toCharArray();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(KeyStoreSupplier.class).to(KeyStoreSupplier.class).in(Singleton.class);
        }
    }
}
