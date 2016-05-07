package com.grpctrl.run;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.rest.ApiApplication;
import com.grpctrl.rest.ContextListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

/**
 * Responsible for running the system.
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    @Nonnull
    private final ConfigSupplier configSupplier;

    /**
     * @param configSupplier the supplier of system configuration properties
     */
    public Runner(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Nonnull
    private ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    /**
     * Run the application server.
     */
    public void run() {
        final Config config = getConfigSupplier().get();
        final String apiHost = config.getString(ConfigKeys.SYSTEM_API_HOST.getKey());
        final int apiPort = config.getInt(ConfigKeys.SYSTEM_API_PORT.getKey());

        final Server server = new Server(new InetSocketAddress(apiHost, apiPort));

        final ServletContextHandler servletContextHandler =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.addEventListener(new ContextListener());
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        final ServletHolder jerseyServlet = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("javax.ws.rs.Application", ApiApplication.class.getName());

        if (config.getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
            final PasswordBasedEncryptionSupplier pbeSupplier =
                    new PasswordBasedEncryptionSupplier(getConfigSupplier());
            final KeyStoreSupplier keyStoreSupplier = new KeyStoreSupplier(getConfigSupplier(), pbeSupplier);
            final SSLContext sslContext =
                    new SslContextSupplier(getConfigSupplier(), keyStoreSupplier, pbeSupplier).get();

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);

            final HttpConfiguration httpConfiguration = new HttpConfiguration();
            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(apiPort);
            httpConfiguration.setOutputBufferSize(32768);
            httpConfiguration.setRequestHeaderSize(8192);
            httpConfiguration.setResponseHeaderSize(8192);
            httpConfiguration.setSendServerVersion(false);
            httpConfiguration.setSendDateHeader(false);

            final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
            httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

            final ServerConnector sslConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpsConfiguration));
            sslConnector.setPort(apiPort);
            server.setConnectors(new Connector[] {sslConnector});
        }

        try {
            server.start();
            server.join();
        } catch (final Throwable throwable) {
            LOG.error("Problem with Jetty", throwable);
        }

        LOG.info("Server started on {}:{}", apiHost, apiPort);
    }

    /**
     * The entry-point into running this system.
     *
     * @param args the command-line parameters
     */
    public static void main(final String... args) {
        new Runner(new ConfigSupplier(ConfigFactory.load().withFallback(ConfigFactory.systemEnvironment()))).run();
    }
}
