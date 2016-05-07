package com.grpctrl.crypto.store;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;

import org.glassfish.hk2.api.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ext.ContextResolver;

/**
 * Provides singleton access to a {@link KeyStore} instance.
 */
abstract class StoreSupplier implements Supplier<KeyStore>, Factory<KeyStore>, ContextResolver<KeyStore> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nonnull
    private final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier;

    @Nullable
    private volatile KeyStore singleton;

    /**
     * @param configSupplier provides access to the static system configuration properties
     * @param passwordBasedEncryptionSupplier provides support for password-based encryption and decryption
     */
    @Inject
    public StoreSupplier(
            @Nonnull final ConfigSupplier configSupplier,
            @Nonnull final PasswordBasedEncryptionSupplier passwordBasedEncryptionSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.passwordBasedEncryptionSupplier = Objects.requireNonNull(passwordBasedEncryptionSupplier);
    }

    /**
     * @return the supplier of the static system configuration properties
     */
    @Nonnull
    protected ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    /**
     * @return provides support for password-based encryption and decryption
     */
    @Nonnull
    protected PasswordBasedEncryptionSupplier getPasswordBasedEncryptionSupplier() {
        return this.passwordBasedEncryptionSupplier;
    }

    /**
     * @return the file from which the {@link KeyStore} should be loaded
     */
    @Nonnull
    protected abstract String getFile();

    /**
     * @return the type of the {@link KeyStore} being loaded
     */
    @Nonnull
    protected abstract String getType();

    /**
     * @return the password used to access the {@link KeyStore}
     */
    @Nonnull
    protected abstract char[] getPassword();

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public KeyStore get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (StoreSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public KeyStore getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public KeyStore provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final KeyStore keyStore) {
        // Nothing to do.
    }

    @Nonnull
    private KeyStore create() {
        try (final FileInputStream fis = new FileInputStream(getFile())) {
            final KeyStore keyStore = KeyStore.getInstance(getType());
            keyStore.load(fis, getPassword());
            return keyStore;
        } catch (final IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException exception) {
            throw new InternalServerErrorException(String.format("Failed to load key store from file %s (with type %s)",
                    new File(getFile()).getAbsolutePath(), getType()), exception);
        }
    }
}
