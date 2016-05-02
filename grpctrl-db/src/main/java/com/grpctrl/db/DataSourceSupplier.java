package com.grpctrl.db;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.config.ConfigSupplier;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

/**
 * Provides singleton access to a {@link DataSource} used to communicate with the configured JDBC database.
 */
public class DataSourceSupplier implements Supplier<DataSource>, Factory<DataSource> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nullable
    private volatile DataSource singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param configSupplier the {@link ConfigSupplier} responsible for providing access to the static system
     * configuration
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public DataSourceSupplier(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Nonnull
    private ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public DataSource get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (DataSourceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public DataSource provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final DataSource dataSource) {
        // No need to do anything here.
    }

    @Nonnull
    private DataSource create() {
        final Config config = getConfigSupplier().get();

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString(ConfigKeys.DB_URL.getKey()));
        hikariConfig.setUsername(config.getString(ConfigKeys.DB_USERNAME.getKey()));
        hikariConfig.setPassword(config.getString(ConfigKeys.DB_PASSWORD.getKey()));
        hikariConfig.setMinimumIdle(config.getInt(ConfigKeys.DB_MINIMUM_IDLE.getKey()));
        hikariConfig.setMaximumPoolSize(config.getInt(ConfigKeys.DB_MAXIMUM_POOL_SIZE.getKey()));
        hikariConfig.setIdleTimeout(config.getDuration(ConfigKeys.DB_TIMEOUT_IDLE.getKey()).toMillis());
        hikariConfig.setConnectionTimeout(config.getDuration(ConfigKeys.DB_TIMEOUT_CONNECTION.getKey()).toMillis());
        hikariConfig.setAutoCommit(false);

        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        if (config.getBoolean(ConfigKeys.DB_CLEAN.getKey())) {
            flyway.clean();
        }
        if (config.getBoolean(ConfigKeys.DB_MIGRATE.getKey())) {
            flyway.migrate();
        }

        return dataSource;
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(DataSourceSupplier.class).to(DataSourceSupplier.class).in(Singleton.class);
            bindFactory(DataSourceSupplier.class).to(DataSource.class).in(Singleton.class);
        }
    }
}
