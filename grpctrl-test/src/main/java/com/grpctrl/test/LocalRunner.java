package com.grpctrl.test;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.run.Runner;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;

/**
 * Run the system locally in an IDE.
 */
public class LocalRunner {
    private static final Logger LOG = LoggerFactory.getLogger(LocalRunner.class);

    public Config getConfig() {
        final ClassLoader classLoader = LocalRunner.class.getClassLoader();
        final Optional<URL> keystore = Optional.ofNullable(classLoader.getResource("keystore.jks"));
        final Optional<URL> truststore = Optional.ofNullable(classLoader.getResource("truststore.jks"));

        final Map<String, ConfigValue> configMap = new HashMap<>();
        if (keystore.isPresent()) {
            configMap.put(ConfigKeys.CRYPTO_SSL_KEYSTORE_FILE.getKey(), fromAnyRef(keystore.get().getFile()));
        }
        if (truststore.isPresent()) {
            configMap.put(ConfigKeys.CRYPTO_SSL_TRUSTSTORE_FILE.getKey(), fromAnyRef(truststore.get().getFile()));
        }
        if (!keystore.isPresent() || !truststore.isPresent()) {
            LOG.warn("Could not find keystore or truststore in src/main/resources, running on HTTP instead of HTTPS.");
            configMap.put(ConfigKeys.CRYPTO_SSL_ENABLED.getKey(), fromAnyRef(false));
        }

        // Make sure a shared secret is available.
        final Map<String, ConfigValue> sharedSecretMap = new HashMap<>();
        sharedSecretMap.put("SHARED_SECRET", fromAnyRef("SHARED_SECRET"));
        final Config sharedSecretConfig = ConfigFactory.parseMap(sharedSecretMap);

        return ConfigFactory.parseMap(configMap).withFallback(ConfigFactory.load())
                .withFallback(ConfigFactory.systemEnvironment()).withFallback(sharedSecretConfig);
    }

    private void run() throws ServletException {
        new Runner(new ConfigSupplier(getConfig())).run();
    }

    /**
     * The entry-point into running the system locally.
     *
     * @param args the command-line parameters
     * @throws Exception if there is a problem launching the service
     */
    public static void main(final String... args) throws Exception {
        new LocalRunner().run();
    }
}
