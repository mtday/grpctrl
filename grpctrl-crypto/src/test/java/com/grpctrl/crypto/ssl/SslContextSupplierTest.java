package com.grpctrl.crypto.ssl;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;
import static org.junit.Assert.assertNotNull;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.crypto.store.TrustStoreSupplier;
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
 * Perform testing on the {@link SslContextSupplier}.
 */
public class SslContextSupplierTest {
    private static SslContextSupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        final Optional<URL> keystore =
                Optional.ofNullable(SslContextSupplierTest.class.getClassLoader().getResource("keystore.jks"));
        final Optional<URL> truststore =
                Optional.ofNullable(SslContextSupplierTest.class.getClassLoader().getResource("truststore.jks"));
        if (!keystore.isPresent()) {
            throw new RuntimeException("Failed to find keystore classpath resource");
        }
        if (!truststore.isPresent()) {
            throw new RuntimeException("Failed to find truststore classpath resource");
        }

        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.CRYPTO_SSL_KEYSTORE_FILE.getKey(), fromAnyRef(keystore.get().getFile()));
        map.put(ConfigKeys.CRYPTO_SSL_TRUSTSTORE_FILE.getKey(), fromAnyRef(truststore.get().getFile()));

        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final ConfigSupplier configSupplier = new ConfigSupplier(config);

        final PasswordBasedEncryptionSupplier pbeSupplier = new PasswordBasedEncryptionSupplier(configSupplier);
        supplier = new SslContextSupplier(configSupplier, new KeyStoreSupplier(configSupplier, pbeSupplier),
                new TrustStoreSupplier(configSupplier, pbeSupplier), pbeSupplier);
    }

    @Test
    public void testNotEnabled() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(ConfigKeys.CRYPTO_SSL_ENABLED.getKey(), fromAnyRef(false));

        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final ConfigSupplier configSupplier = new ConfigSupplier(config);

        final PasswordBasedEncryptionSupplier pbeSupplier = new PasswordBasedEncryptionSupplier(configSupplier);
        assertNotNull(new SslContextSupplier(configSupplier, new KeyStoreSupplier(configSupplier, pbeSupplier),
                new TrustStoreSupplier(configSupplier, pbeSupplier), pbeSupplier).get());
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
        new SslContextSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
