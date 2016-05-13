package com.grpctrl.security;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.ExecutorServiceSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to the login service that performs security management in Jetty.
 */
@Provider
public class CustomLoginServiceSupplier
        implements Supplier<CustomLoginService>, Factory<CustomLoginService>, ContextResolver<CustomLoginService> {
    @Nonnull
    private final ExecutorServiceSupplier executorServiceSupplier;
    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;
    @Nonnull
    private final UserDaoSupplier userDaoSupplier;

    @Nullable
    private volatile CustomLoginService singleton = null;

    @Inject
    public CustomLoginServiceSupplier(
            @Nonnull final ExecutorServiceSupplier executorServiceSupplier,
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier,
            @Nonnull final UserDaoSupplier userDaoSupplier) {
        this.executorServiceSupplier = Objects.requireNonNull(executorServiceSupplier);
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);
        this.userDaoSupplier = Objects.requireNonNull(userDaoSupplier);
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public CustomLoginService get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (CustomLoginServiceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public CustomLoginService getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public CustomLoginService provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final CustomLoginService loginService) {
        // Nothing to do.
    }

    @Nonnull
    private CustomLoginService create() {
        return new CustomLoginService(
                this.executorServiceSupplier, this.configSupplier, this.objectMapperSupplier,
                this.oAuth20ServiceSupplier, this.userDaoSupplier);
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(CustomLoginServiceSupplier.class).to(CustomLoginServiceSupplier.class).in(Singleton.class);
        }
    }
}
