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

import javax.ws.rs.InternalServerErrorException;

/**
 * Perform testing on the {@link com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier}.
 */
public class PasswordBasedEncryptionSupplierTest {
    private static com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        supplier = new com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier(new ConfigSupplier());
    }

    @Test(expected = InternalServerErrorException.class)
    public void testNoSharedSecret() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.CRYPTO_SHARED_SECRET_VARIABLE.getKey(), fromAnyRef("DOES_NOT_EXIST"));
        final ConfigSupplier configSupplier = new ConfigSupplier(ConfigFactory.parseMap(map));
        new com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier(configSupplier).get();
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
        new com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
