package com.grpctrl.common.supplier;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to the static system configuration properties.
 */
@Provider
@Singleton
public class ConfigSupplier implements Supplier<Config>, Factory<Config>, ContextResolver<Config> {
    @Nullable
    private volatile Config singleton;

    /**
     * Default constructor.
     */
    public ConfigSupplier() {
    }

    /**
     * @param config the static system configuration properties to supply
     */
    public ConfigSupplier(@Nonnull final Config config) {
        this.singleton = Objects.requireNonNull(config);
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public Config get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ConfigSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public Config getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public Config provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final Config config) {
        // No need to do anything here.
    }

    @Nonnull
    private Config create() {
        return ConfigFactory.load().withFallback(ConfigFactory.systemEnvironment());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ConfigSupplier.class).to(ConfigSupplier.class).in(Singleton.class);
        }
    }
}
