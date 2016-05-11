package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.impl.PostgresAccountDao;

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
 * Provides singleton access to an {@link AccountDao} used to communicate with the configured JDBC database for account
 * information.
 */
@Provider
public class AccountDaoSupplier implements Supplier<AccountDao>, Factory<AccountDao>, ContextResolver<AccountDao> {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final ServiceLevelDaoSupplier serviceLevelDaoSupplier;
    @Nonnull
    private final ApiLoginDaoSupplier apiLoginDaoSupplier;

    @Nullable
    private volatile AccountDao singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param dataSourceSupplier the {@link DataSourceSupplier} responsible for providing access to a configured
     *     data source used to communicate with the JDBC database
     * @param serviceLevelDaoSupplier the {@link ServiceLevelDaoSupplier} used to manage the service level objects
     * @param apiLoginDaoSupplier the {@link ApiLoginDaoSupplier} used to manage the API login objects
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public AccountDaoSupplier(
            @Nonnull final DataSourceSupplier dataSourceSupplier,
            @Nonnull final ServiceLevelDaoSupplier serviceLevelDaoSupplier,
            @Nonnull final ApiLoginDaoSupplier apiLoginDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.serviceLevelDaoSupplier = Objects.requireNonNull(serviceLevelDaoSupplier);
        this.apiLoginDaoSupplier = Objects.requireNonNull(apiLoginDaoSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private ServiceLevelDaoSupplier getServiceLevelDaoSupplier() {
        return this.serviceLevelDaoSupplier;
    }

    @Nonnull
    private ApiLoginDaoSupplier getApiLoginDaoSupplier() {
        return this.apiLoginDaoSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public AccountDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (AccountDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public AccountDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public AccountDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final AccountDao accountDao) {
        // No need to do anything here.
    }

    @Nonnull
    private AccountDao create() {
        return new PostgresAccountDao(getDataSourceSupplier(), getServiceLevelDaoSupplier(), getApiLoginDaoSupplier());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(AccountDaoSupplier.class).to(AccountDaoSupplier.class).in(Singleton.class);
            bindFactory(AccountDaoSupplier.class).to(AccountDao.class).in(Singleton.class);
        }
    }
}
