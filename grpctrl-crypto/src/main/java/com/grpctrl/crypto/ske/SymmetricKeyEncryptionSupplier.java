package com.grpctrl.crypto.ske;

import com.google.common.base.Charsets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.crypto.EncryptionException;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ske.impl.AESSymmetricKeyEncryption;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.typesafe.config.Config;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link SymmetricKeyEncryption} implementation.
 */
@Provider
public class SymmetricKeyEncryptionSupplier
        implements Supplier<SymmetricKeyEncryption>, Factory<SymmetricKeyEncryption>,
        ContextResolver<SymmetricKeyEncryption> {
    @Nonnull
    private final Config config;

    @Nonnull
    private final KeyStoreSupplier keyStoreSupplier;

    @Nonnull
    private final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier;

    @Nullable
    private volatile SymmetricKeyEncryption singleton = null;

    /**
     * @param config provides access to the static system configuration properties
     * @param keyStoreSupplier provides access to the system key store
     * @param passwordBasedEncryptionSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public SymmetricKeyEncryptionSupplier(
            @Nonnull final Config config, @Nonnull final KeyStoreSupplier keyStoreSupplier,
            @Nonnull final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier) {
        this.config = Objects.requireNonNull(config);
        this.keyStoreSupplier = Objects.requireNonNull(keyStoreSupplier);
        this.passwordBasedEncryptionSupplier = Objects.requireNonNull(passwordBasedEncryptionSupplier);
    }

    /**
     * @return the static system configuration properties
     */
    @Nonnull
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return provides access to the system key store
     */
    @Nonnull
    protected KeyStoreSupplier getKeyStoreSupplier() {
        return this.keyStoreSupplier;
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
    public SymmetricKeyEncryption get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (SymmetricKeyEncryptionSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public SymmetricKeyEncryption getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public SymmetricKeyEncryption provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final SymmetricKeyEncryption symmetricKeyEncryption) {
        // Nothing to do.
    }

    /**
     * @return a {@link KeyPair} representing the system public and private keys
     * @throws EncryptionException if there is a problem retrieving the key pair
     */
    @Nonnull
    protected KeyPair getSymmetricKeyPair() throws EncryptionException {
        try {
            final KeyStore keyStore = getKeyStoreSupplier().get();
            final String alias = keyStore.aliases().nextElement();
            final String encryptedPassword = getConfig().getString(ConfigKeys.CRYPTO_SSL_KEYSTORE_PASSWORD.getKey());
            final char[] decryptedPassword =
                    getPasswordBasedEncryptionSupplier().get().decryptProperty(encryptedPassword, Charsets.UTF_8)
                            .toCharArray();
            final Key key = keyStore.getKey(alias, decryptedPassword);
            return new KeyPair(keyStore.getCertificate(alias).getPublicKey(), (PrivateKey) key);
        } catch (final EncryptionException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new EncryptionException("Failed to retrieve symmetric keys from system key store", exception);
        }
    }

    @Nonnull
    private SymmetricKeyEncryption create() {
        return new AESSymmetricKeyEncryption(getSymmetricKeyPair());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SymmetricKeyEncryptionSupplier.class).to(SymmetricKeyEncryptionSupplier.class).in(Singleton.class);
            bindFactory(SymmetricKeyEncryptionSupplier.class).to(SymmetricKeyEncryption.class).in(Singleton.class);
        }
    }
}
