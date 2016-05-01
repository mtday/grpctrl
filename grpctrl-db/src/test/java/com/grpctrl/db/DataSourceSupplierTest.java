package com.grpctrl.db;

import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.config.ConfigSupplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link DataSourceSupplier}.
 */
public class DataSourceSupplierTest {
    private static DataSourceSupplier supplier;

    @BeforeClass
    public static void beforeClass() {
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

        supplier = new DataSourceSupplier(configSupplier);
    }

    @Test
    public void testGet() {
        assertNotNull(supplier.get());
    }

    @Test
    public void testProvide() {
        assertNotNull(supplier.provide());
    }

    @Test
    public void testDispose() {
        // Nothing to really test here.
        supplier.dispose(supplier.get());
    }

    @Test
    public void testBinder() {
        // Nothing to really test here.
        new DataSourceSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
