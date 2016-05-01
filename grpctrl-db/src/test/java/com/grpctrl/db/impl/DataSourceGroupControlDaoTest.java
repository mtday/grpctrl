package com.grpctrl.db.impl;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.config.ConfigSupplier;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.GroupControlDao;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link DataSourceGroupControlDao} class using an in-memory HSQLDB database.
 */
public class DataSourceGroupControlDaoTest extends BaseGroupControlDaoTest {
    private static DataSourceSupplier dataSourceSupplier;

    @BeforeClass
    public static void setup() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.DB_URL.getKey(), ConfigValueFactory.fromAnyRef("jdbc:hsqldb:mem:grpctrl"));
        map.put(ConfigKeys.DB_USERNAME.getKey(), ConfigValueFactory.fromAnyRef("SA"));
        map.put(ConfigKeys.DB_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef(""));
        map.put(ConfigKeys.DB_MINIMUM_IDLE.getKey(), ConfigValueFactory.fromAnyRef(10));
        map.put(ConfigKeys.DB_MAXIMUM_POOL_SIZE.getKey(), ConfigValueFactory.fromAnyRef(10));
        map.put(ConfigKeys.DB_TIMEOUT_IDLE.getKey(), ConfigValueFactory.fromAnyRef("10 minutes"));
        map.put(ConfigKeys.DB_TIMEOUT_CONNECTION.getKey(), ConfigValueFactory.fromAnyRef("10 seconds"));

        final Config config = ConfigFactory.parseMap(map);

        final ConfigSupplier configSupplier = Mockito.mock(ConfigSupplier.class);
        Mockito.when(configSupplier.get()).thenReturn(config);

        dataSourceSupplier = new DataSourceSupplier(configSupplier);
    }

    @Override
    public GroupControlDao getGroupControlDao() {
        return new DataSourceGroupControlDao(dataSourceSupplier);
    }
}
