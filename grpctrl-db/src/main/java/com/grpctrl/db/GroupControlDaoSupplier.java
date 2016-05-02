package com.grpctrl.db;

import com.grpctrl.db.impl.PostgresGroupControlDao;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides singleton access to a {@link GroupControlDao} used to communicate with the configured JDBC database.
 */
public class GroupControlDaoSupplier implements Supplier<GroupControlDao>, Factory<GroupControlDao> {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;

    @Nullable
    private volatile GroupControlDao singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param dataSourceSupplier the {@link DataSourceSupplier} responsible for providing access to a configured
     * data source used to communicate with the JDBC database
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public GroupControlDaoSupplier(@Nonnull final DataSourceSupplier dataSourceSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
    }

    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public GroupControlDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (GroupControlDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public GroupControlDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final GroupControlDao tagDao) {
        // No need to do anything here.
    }

    @Nonnull
    private GroupControlDao create() {
        return new PostgresGroupControlDao(getDataSourceSupplier());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(GroupControlDaoSupplier.class).to(GroupControlDaoSupplier.class).in(Singleton.class);
            bindFactory(GroupControlDaoSupplier.class).to(GroupControlDao.class).in(Singleton.class);
        }
    }
}
