package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.dao.UserEmailDao;
import com.grpctrl.db.dao.impl.PostgresUserEmailDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link UserEmailDao} used to communicate with the configured JDBC database for
 * user email information.
 */
@Provider
public class UserEmailDaoSupplier
        implements Supplier<UserEmailDao>, Factory<UserEmailDao>, ContextResolver<UserEmailDao> {
    @Nullable
    private volatile UserEmailDao singleton;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public UserEmailDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (UserEmailDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public UserEmailDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public UserEmailDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final UserEmailDao userEmailDao) {
        // No need to do anything here.
    }

    @Nonnull
    private UserEmailDao create() {
        return new PostgresUserEmailDao();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(UserEmailDaoSupplier.class).to(UserEmailDaoSupplier.class).in(Singleton.class);
            bindFactory(UserEmailDaoSupplier.class).to(UserEmailDao.class).in(Singleton.class);
        }
    }
}
