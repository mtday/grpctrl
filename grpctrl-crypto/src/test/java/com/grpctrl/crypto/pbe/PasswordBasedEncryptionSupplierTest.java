package com.grpctrl.crypto.pbe;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;
import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link PasswordBasedEncryptionSupplier}.
 */
public class PasswordBasedEncryptionSupplierTest {
    private static PasswordBasedEncryptionSupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        supplier = new PasswordBasedEncryptionSupplier(new ConfigSupplier());
    }

    @Test
    public void testNoSharedSecret() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.CRYPTO_SHARED_SECRET_VARIABLE.getKey(), fromAnyRef("DOES_NOT_EXIST"));
        map.put(ConfigKeys.CRYPTO_SHARED_SECRET_DEFAULT.getKey(), fromAnyRef("password"));
        final ConfigSupplier configSupplier = new ConfigSupplier(ConfigFactory.parseMap(map));
        new PasswordBasedEncryptionSupplier(configSupplier).get();
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
        new PasswordBasedEncryptionSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
