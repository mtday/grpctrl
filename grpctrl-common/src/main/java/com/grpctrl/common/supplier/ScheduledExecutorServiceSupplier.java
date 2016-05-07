package com.grpctrl.common.supplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a executor service that can schedule and run background tasks.
 */
@Provider
public class ScheduledExecutorServiceSupplier
        implements Supplier<ScheduledExecutorService>, Factory<ScheduledExecutorService>, ContextResolver<ScheduledExecutorService> {
    @Nullable
    private volatile ScheduledExecutorService singleton = null;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public ScheduledExecutorService get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ScheduledExecutorServiceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public ScheduledExecutorService getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public ScheduledExecutorService provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final ScheduledExecutorService scheduledExecutorService) {
        // Nothing to do.
    }

    @Nonnull
    private ScheduledExecutorService create() {
        return Executors.newScheduledThreadPool(3);
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ScheduledExecutorServiceSupplier.class).to(ScheduledExecutorServiceSupplier.class).in(Singleton.class);
            bindFactory(ScheduledExecutorServiceSupplier.class).to(ScheduledExecutorService.class).in(Singleton.class);
        }
    }
}
