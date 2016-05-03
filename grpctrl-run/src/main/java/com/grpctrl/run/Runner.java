package com.grpctrl.run;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.crypto.store.TrustStoreSupplier;
import com.grpctrl.rest.ApiApplication;
import com.grpctrl.rest.ContextListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;

/**
 * Responsible for running the system.
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    private static final String ROOT = "/";

    @Nonnull
    private final Config config;

    /**
     * @param config the system configuration properties
     */
    public Runner(@Nonnull final Config config) {
        this.config = Objects.requireNonNull(config);
    }

    @Nonnull
    private Config getConfig() {
        return this.config;
    }

    /**
     * Run the application server.
     *
     * @throws ServletException if there is a problem running the application server
     */
    public void run() throws ServletException {
        final ServletInfo api =
                Servlets.servlet(ApiApplication.class.getSimpleName(), ServletContainer.class).setLoadOnStartup(1)
                        .addMapping("/api/*").addInitParam("javax.ws.rs.Application", ApiApplication.class.getName());

        final DeploymentInfo deploymentInfo =
                Servlets.deployment().setClassLoader(Runner.class.getClassLoader()).setContextPath(ROOT)
                        .setDeploymentName(ApiApplication.class.getSimpleName())
                        .addListener(new ListenerInfo(ContextListener.class)).addServlets(api);

        final DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();

        final PathHandler pathHandler =
                Handlers.path(Handlers.redirect(ROOT)).addPrefixPath(ROOT, deploymentManager.start());

        final EncodingHandler encodingHandler = new EncodingHandler(new ContentEncodingRepository()
                .addEncodingHandler("gzip", new GzipEncodingProvider(), 50, Predicates.parse("max-content-size(5)")))
                .setNext(pathHandler);

        final String apiHost = getConfig().getString(ConfigKeys.SYSTEM_API_HOST.getKey());
        final int apiPort = getConfig().getInt(ConfigKeys.SYSTEM_API_PORT.getKey());

        if (getConfig().getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
            final PasswordBasedEncryptionSupplier pbeSupplier = new PasswordBasedEncryptionSupplier(getConfig());
            final KeyStoreSupplier keyStoreSupplier = new KeyStoreSupplier(getConfig(), pbeSupplier);
            final TrustStoreSupplier trustStoreSupplier = new TrustStoreSupplier(getConfig(), pbeSupplier);
            final SSLContext sslContext =
                    new SslContextSupplier(getConfig(), keyStoreSupplier, trustStoreSupplier, pbeSupplier).get();
            Undertow.builder().addHttpsListener(apiPort, apiHost, sslContext).setHandler(encodingHandler).build()
                    .start();
        } else {
            // Running in insecure non-HTTPS mode.
            Undertow.builder().addHttpListener(apiPort, apiHost).setHandler(encodingHandler).build().start();
        }

        LOG.info("Server started on {}:{}", apiHost, apiPort);
    }

    /**
     * The entry-point into running this system.
     *
     * @param args the command-line parameters
     * @throws Exception if there is a problem launching the service
     */
    public static void main(final String... args) throws Exception {
        new Runner(ConfigFactory.load()).run();
    }
}
