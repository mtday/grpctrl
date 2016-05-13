package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.dao.UserAuthDao;
import com.grpctrl.db.dao.impl.PostgresUserAuthDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link UserAuthDao} used to communicate with the configured JDBC database for
 * user authorization information.
 */
@Provider
public class UserAuthDaoSupplier
        implements Supplier<UserAuthDao>, Factory<UserAuthDao>, ContextResolver<UserAuthDao> {
    @Nullable
    private volatile UserAuthDao singleton;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public UserAuthDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (UserAuthDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public UserAuthDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public UserAuthDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final UserAuthDao userAuthDao) {
        // No need to do anything here.
    }

    @Nonnull
    private UserAuthDao create() {
        return new PostgresUserAuthDao();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(UserAuthDaoSupplier.class).to(UserAuthDaoSupplier.class).in(Singleton.class);
        }
    }
}
