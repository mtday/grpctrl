package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.UserDao;
import com.grpctrl.db.dao.impl.PostgresUserDao;

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
 * Provides singleton access to an {@link UserDao} used to communicate with the configured JDBC database for user
 * information.
 */
@Provider
public class UserDaoSupplier implements Supplier<UserDao>, Factory<UserDao>, ContextResolver<UserDao> {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final UserAuthDaoSupplier userAuthDaoSupplier;
    @Nonnull
    private final UserEmailDaoSupplier userEmailDaoSupplier;
    @Nonnull
    private final UserRoleDaoSupplier userRoleDaoSupplier;
    @Nonnull
    private final AccountDaoSupplier accountDaoSupplier;

    @Nullable
    private volatile UserDao singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param dataSourceSupplier the {@link DataSourceSupplier} responsible for providing access to a configured
     *     data source used to communicate with the JDBC database
     * @param userAuthDaoSupplier the {@link UserAuthDaoSupplier} used to manage the user auth objects
     * @param userEmailDaoSupplier the {@link UserEmailDaoSupplier} used to manage the user email objects
     * @param userRoleDaoSupplier the {@link UserRoleDaoSupplier} used to manage the user role objects
     * @param accountDaoSupplier the {@link AccountDaoSupplier} used to manage the account objects
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public UserDaoSupplier(
            @Nonnull final DataSourceSupplier dataSourceSupplier,
            @Nonnull final UserAuthDaoSupplier userAuthDaoSupplier,
            @Nonnull final UserEmailDaoSupplier userEmailDaoSupplier,
            @Nonnull final UserRoleDaoSupplier userRoleDaoSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.userAuthDaoSupplier = Objects.requireNonNull(userAuthDaoSupplier);
        this.userEmailDaoSupplier = Objects.requireNonNull(userEmailDaoSupplier);
        this.userRoleDaoSupplier = Objects.requireNonNull(userRoleDaoSupplier);
        this.accountDaoSupplier = Objects.requireNonNull(accountDaoSupplier);
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public UserDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (UserDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public UserDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public UserDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final UserDao userDao) {
        // No need to do anything here.
    }

    @Nonnull
    private UserDao create() {
        return new PostgresUserDao(this.dataSourceSupplier, this.userAuthDaoSupplier, this.userEmailDaoSupplier,
                this.userRoleDaoSupplier, this.accountDaoSupplier);
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(UserDaoSupplier.class).to(UserDaoSupplier.class).in(Singleton.class);
        }
    }
}
