package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.dao.UserRoleDao;
import com.grpctrl.db.dao.impl.PostgresUserRoleDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link UserRoleDao} used to communicate with the configured JDBC database for
 * user role information.
 */
@Provider
public class UserRoleDaoSupplier
        implements Supplier<UserRoleDao>, Factory<UserRoleDao>, ContextResolver<UserRoleDao> {
    @Nullable
    private volatile UserRoleDao singleton;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public UserRoleDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (UserRoleDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public UserRoleDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public UserRoleDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final UserRoleDao userRoleDao) {
        // No need to do anything here.
    }

    @Nonnull
    private UserRoleDao create() {
        return new PostgresUserRoleDao();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(UserRoleDaoSupplier.class).to(UserRoleDaoSupplier.class).in(Singleton.class);
        }
    }
}
