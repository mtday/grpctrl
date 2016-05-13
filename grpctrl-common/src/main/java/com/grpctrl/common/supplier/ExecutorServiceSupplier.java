package com.grpctrl.common.supplier;

import com.grpctrl.common.config.ConfigKeys;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a executor service that can schedule and run background tasks.
 */
@Provider
public class ExecutorServiceSupplier
        implements Supplier<ExecutorService>, Factory<ExecutorService>, ContextResolver<ExecutorService> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nullable
    private volatile ExecutorService singleton = null;

    /**
     * @param configSupplier provides access to the static system configuration properties
     */
    @Inject
    public ExecutorServiceSupplier(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public ExecutorService get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ExecutorServiceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public ExecutorService getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public ExecutorService provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final ExecutorService executorService) {
        // Nothing to do.
    }

    @Nonnull
    private ExecutorService create() {
        return Executors.newFixedThreadPool(this.configSupplier.get().getInt(ConfigKeys.SECURITY_THREADS.getKey()));
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ExecutorServiceSupplier.class).to(ExecutorServiceSupplier.class).in(Singleton.class);
        }
    }
}
