package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.dao.ServiceLevelDao;
import com.grpctrl.db.dao.impl.PostgresServiceLevelDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link ServiceLevelDao} used to communicate with the configured JDBC database for
 * service level information.
 */
@Provider
public class ServiceLevelDaoSupplier
        implements Supplier<ServiceLevelDao>, Factory<ServiceLevelDao>, ContextResolver<ServiceLevelDao> {
    @Nullable
    private volatile ServiceLevelDao singleton;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public ServiceLevelDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ServiceLevelDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public ServiceLevelDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public ServiceLevelDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final ServiceLevelDao serviceLevelDao) {
        // No need to do anything here.
    }

    @Nonnull
    private ServiceLevelDao create() {
        return new PostgresServiceLevelDao();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ServiceLevelDaoSupplier.class).to(ServiceLevelDaoSupplier.class).in(Singleton.class);
        }
    }
}
