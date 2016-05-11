package com.grpctrl.db.dao.supplier;

import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.db.DataSourceSupplier;
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
 * Perform testing on the {@link AccountDaoSupplier}.
 */
public class AccountDaoSupplierTest {
    private static AccountDaoSupplier supplier;

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
        map.put(ConfigKeys.DB_CLEAN.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(ConfigKeys.DB_MIGRATE.getKey(), ConfigValueFactory.fromAnyRef("false"));

        map.put(ConfigKeys.CRYPTO_SHARED_SECRET_VARIABLE.getKey(), ConfigValueFactory.fromAnyRef("SHARED_SECRET"));
        map.put("SHARED_SECRET", ConfigValueFactory.fromAnyRef("SHARED_SECRET"));

        final Config config = ConfigFactory.parseMap(map);

        final ConfigSupplier configSupplier = Mockito.mock(ConfigSupplier.class);
        Mockito.when(configSupplier.get()).thenReturn(config);

        final ServiceLevelDaoSupplier serviceLevelDaoSupplier = new ServiceLevelDaoSupplier();
        final ApiLoginDaoSupplier apiLoginDaoSupplier = new ApiLoginDaoSupplier();
        supplier = new AccountDaoSupplier(
                new DataSourceSupplier(configSupplier, new PasswordBasedEncryptionSupplier(configSupplier)),
                serviceLevelDaoSupplier, apiLoginDaoSupplier);
    }

    @Test
    public void testGet() {
        assertNotNull(supplier.get());
    }

    @Test
    public void testGetContext() {
        assertNotNull(supplier.getContext(getClass()));
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
        new AccountDaoSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
