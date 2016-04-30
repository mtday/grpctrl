package com.grpctrl.service;

import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.config.ConfigSupplier;
import com.grpctrl.common.model.ServiceType;
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
 * Perform testing on the {@link GroupControlServiceSupplier}.
 */
public class GroupControlServiceSupplierTest {
    private static GroupControlServiceSupplier memorySupplier;
    private static GroupControlServiceSupplier databaseSupplier;

    @BeforeClass
    public static void beforeClass() {
        final Map<String, ConfigValue> memoryMap = new HashMap<>();
        memoryMap.put(ConfigKeys.SERVICE_IMPL.getKey(), ConfigValueFactory.fromAnyRef(ServiceType.MEMORY.name()));

        final Map<String, ConfigValue> databaseMap = new HashMap<>();
        databaseMap.put(ConfigKeys.SERVICE_IMPL.getKey(), ConfigValueFactory.fromAnyRef(ServiceType.DATABASE.name()));

        final Config memoryConfig = ConfigFactory.parseMap(memoryMap);
        final Config databaseConfig = ConfigFactory.parseMap(databaseMap);

        final ConfigSupplier memoryConfigSupplier = Mockito.mock(ConfigSupplier.class);
        Mockito.when(memoryConfigSupplier.get()).thenReturn(memoryConfig);

        final ConfigSupplier databaseConfigSupplier = Mockito.mock(ConfigSupplier.class);
        Mockito.when(databaseConfigSupplier.get()).thenReturn(databaseConfig);

        memorySupplier = new GroupControlServiceSupplier(memoryConfigSupplier);
        databaseSupplier = new GroupControlServiceSupplier(databaseConfigSupplier);
    }

    @Test
    public void testGet() {
        assertNotNull(memorySupplier.get());
        assertNotNull(databaseSupplier.get());
    }

    @Test
    public void testProvide() {
        assertNotNull(memorySupplier.provide());
        assertNotNull(databaseSupplier.provide());
    }

    @Test
    public void testDispose() {
        // Nothing to really test here.
        memorySupplier.dispose(memorySupplier.get());
        databaseSupplier.dispose(databaseSupplier.get());
    }

    @Test
    public void testBinder() {
        // Nothing to really test here.
        new GroupControlServiceSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
