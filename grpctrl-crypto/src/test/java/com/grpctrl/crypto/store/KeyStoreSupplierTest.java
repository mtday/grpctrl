package com.grpctrl.crypto.store;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;
import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Perform testing on the {@link KeyStoreSupplier}.
 */
public class KeyStoreSupplierTest {
    private static KeyStoreSupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        final Optional<URL> keystore =
                Optional.ofNullable(KeyStoreSupplierTest.class.getClassLoader().getResource("keystore.jks"));
        if (!keystore.isPresent()) {
            throw new RuntimeException("Failed to find keystore");
        }

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.CRYPTO_SSL_KEYSTORE_FILE.getKey(), fromAnyRef(keystore.get().getFile()));

        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final ConfigSupplier configSupplier = new ConfigSupplier(config);

        supplier = new KeyStoreSupplier(configSupplier, new PasswordBasedEncryptionSupplier(configSupplier));
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
        new KeyStoreSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
