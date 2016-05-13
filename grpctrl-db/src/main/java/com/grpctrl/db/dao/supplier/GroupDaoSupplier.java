package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.dao.impl.PostgresGroupDao;

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
 * Provides singleton access to a {@link GroupDao} used to communicate with the configured JDBC database.
 */
@Provider
public class GroupDaoSupplier
        implements Supplier<GroupDao>, Factory<GroupDao>, ContextResolver<GroupDao> {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final TagDaoSupplier tagDaoSupplier;

    @Nullable
    private volatile GroupDao singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param dataSourceSupplier the {@link DataSourceSupplier} responsible for providing access to a configured
     *     data source used to communicate with the JDBC database
     * @param tagDaoSupplier the {@link TagDaoSupplier} used to perform operations on tag data
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public GroupDaoSupplier(@Nonnull final DataSourceSupplier dataSourceSupplier, @Nonnull final TagDaoSupplier tagDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.tagDaoSupplier = Objects.requireNonNull(tagDaoSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private TagDaoSupplier getTagDaoSupplier() {
        return this.tagDaoSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public GroupDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (GroupDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public GroupDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public GroupDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final GroupDao groupDao) {
        // No need to do anything here.
    }

    @Nonnull
    private GroupDao create() {
        return new PostgresGroupDao(getDataSourceSupplier(), getTagDaoSupplier());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(GroupDaoSupplier.class).to(GroupDaoSupplier.class).in(Singleton.class);
        }
    }
}
