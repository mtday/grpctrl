package com.grpctrl.db.impl;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.GroupControlDao;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Perform testing on the {@link PostgresGroupControlDao} class. This is an integration test because it expects a
 * live PostgreSQL server to be up and running.
 */
public class PostgresGroupControlDaoIT extends BaseGroupControlDaoTest {
    private static DataSourceSupplier dataSourceSupplier;

    @BeforeClass
    public static void setup() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.DB_URL.getKey(), ConfigValueFactory.fromAnyRef("jdbc:postgresql://localhost:5432/grpctrl"));
        map.put(ConfigKeys.DB_USERNAME.getKey(), ConfigValueFactory.fromAnyRef("grpctrl"));
        map.put(ConfigKeys.DB_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("password"));
        map.put(ConfigKeys.DB_MINIMUM_IDLE.getKey(), ConfigValueFactory.fromAnyRef(10));
        map.put(ConfigKeys.DB_MAXIMUM_POOL_SIZE.getKey(), ConfigValueFactory.fromAnyRef(10));
        map.put(ConfigKeys.DB_TIMEOUT_IDLE.getKey(), ConfigValueFactory.fromAnyRef("10 minutes"));
        map.put(ConfigKeys.DB_TIMEOUT_CONNECTION.getKey(), ConfigValueFactory.fromAnyRef("10 seconds"));
        map.put(ConfigKeys.DB_TIMEOUT_CONNECTION.getKey(), ConfigValueFactory.fromAnyRef("10 seconds"));
        map.put(ConfigKeys.DB_CLEAN.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(ConfigKeys.DB_MIGRATE.getKey(), ConfigValueFactory.fromAnyRef("true"));

        final Config config = ConfigFactory.parseMap(map);

        final ConfigSupplier configSupplier = Mockito.mock(ConfigSupplier.class);
        Mockito.when(configSupplier.get()).thenReturn(config);

        dataSourceSupplier = new DataSourceSupplier(configSupplier);
    }

    @Override
    public GroupControlDao getGroupControlDao() {
        return new PostgresGroupControlDao(dataSourceSupplier);
    }

    @Override
    public GroupControlDao getGroupControlDaoWithDataSourceException() {
        try {
            final DataSource mockDataSource = Mockito.mock(DataSource.class);
            Mockito.when(mockDataSource.getConnection()).thenThrow(new SQLException("Fake"));

            final DataSourceSupplier mockDataSourceSupplier = Mockito.mock(DataSourceSupplier.class);
            Mockito.when(mockDataSourceSupplier.get()).thenReturn(mockDataSource);

            return new PostgresGroupControlDao(mockDataSourceSupplier);
        } catch (final SQLException fake) {
            throw new RuntimeException("Fake");
        }
    }
}
