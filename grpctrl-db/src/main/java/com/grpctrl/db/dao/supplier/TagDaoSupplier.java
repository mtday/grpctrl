package com.grpctrl.db.dao.supplier;

import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.dao.impl.PostgresTagDao;

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
 * Provides singleton access to a {@link TagDao} used to communicate with the configured JDBC database for tag
 * information.
 */
@Provider
public class TagDaoSupplier implements Supplier<TagDao>, Factory<TagDao>, ContextResolver<TagDao> {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;

    @Nullable
    private volatile TagDao singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param dataSourceSupplier the {@link DataSourceSupplier} responsible for providing access to a configured
     *     data source used to communicate with the JDBC database
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public TagDaoSupplier(
            @Nonnull final DataSourceSupplier dataSourceSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public TagDao get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (TagDaoSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public TagDao getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public TagDao provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final TagDao tagDao) {
        // No need to do anything here.
    }

    @Nonnull
    private TagDao create() {
        return new PostgresTagDao(getDataSourceSupplier());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(TagDaoSupplier.class).to(TagDaoSupplier.class).in(Singleton.class);
            bindFactory(TagDaoSupplier.class).to(TagDao.class).in(Singleton.class);
        }
    }
}
