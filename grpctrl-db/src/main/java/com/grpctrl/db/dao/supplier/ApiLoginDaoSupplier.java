package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.dao.ApiLoginDao;
import com.grpctrl.db.dao.impl.PostgresApiLoginDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link ApiLoginDao} used to communicate with the configured JDBC database for
 * API login information.
 */
@Provider
public class ApiLoginDaoSupplier
        implements Supplier<ApiLoginDao>, Factory<ApiLoginDao>, ContextResolver<ApiLoginDao> {
    @Nullable
    private volatile ApiLoginDao singleton;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public ApiLoginDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ApiLoginDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public ApiLoginDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public ApiLoginDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final ApiLoginDao apiLoginDao) {
        // No need to do anything here.
    }

    @Nonnull
    private ApiLoginDao create() {
        return new PostgresApiLoginDao();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ApiLoginDaoSupplier.class).to(ApiLoginDaoSupplier.class).in(Singleton.class);
        }
    }
}
