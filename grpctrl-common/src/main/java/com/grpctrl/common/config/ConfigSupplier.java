package com.grpctrl.common.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Provides singleton access to the static system configuration properties.
 */
public class ConfigSupplier implements Supplier<Config>, Factory<Config> {
    @Nullable
    private volatile Config singleton;

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
    public Config provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final Config config) {
        // No need to do anything here.
    }

    @Nonnull
    private Config create() {
        return ConfigFactory.load();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ConfigSupplier.class).to(ConfigSupplier.class).in(Singleton.class);
            bindFactory(ConfigSupplier.class).to(Config.class).in(Singleton.class);
        }
    }
}
