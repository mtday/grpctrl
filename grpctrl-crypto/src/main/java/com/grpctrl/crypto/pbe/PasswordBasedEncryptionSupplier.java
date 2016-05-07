package com.grpctrl.crypto.pbe;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption;
import com.typesafe.config.Config;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link PasswordBasedEncryption} implementation.
 */
@Provider
public class PasswordBasedEncryptionSupplier
        implements Supplier<PasswordBasedEncryption>, Factory<PasswordBasedEncryption>,
        ContextResolver<PasswordBasedEncryption> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nullable
    private volatile PasswordBasedEncryption singleton = null;

    /**
     * @param configSupplier provides access to the static system configuration properties
     */
    @Inject
    public PasswordBasedEncryptionSupplier(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    /**
     * @return the static system configuration properties
     */
    @Nonnull
    protected ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public PasswordBasedEncryption get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (PasswordBasedEncryptionSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public PasswordBasedEncryption getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public PasswordBasedEncryption provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final PasswordBasedEncryption passwordBasedEncryption) {
        // Nothing to do.
    }

    /**
     * @return the shared secret defined for the system
     */
    @Nonnull
    private String getSharedSecret() {
        final Config config = getConfigSupplier().get();
        final String sharedSecretVar = config.getString(ConfigKeys.CRYPTO_SHARED_SECRET_VARIABLE.getKey());
        if (config.hasPath(sharedSecretVar)) {
            return config.getString(sharedSecretVar);
        }
        throw new InternalServerErrorException("Failed to retrieve shared secret from variable " + sharedSecretVar);
    }

    @Nonnull
    private PasswordBasedEncryption create() {
        return new AESPasswordBasedEncryption(getSharedSecret().toCharArray());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(PasswordBasedEncryptionSupplier.class).to(PasswordBasedEncryptionSupplier.class).in(Singleton.class);
            bindFactory(PasswordBasedEncryptionSupplier.class).to(PasswordBasedEncryption.class).in(Singleton.class);
        }
    }
}
