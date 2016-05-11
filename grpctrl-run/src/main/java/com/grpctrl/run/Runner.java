package com.grpctrl.run;

import com.google.common.collect.Sets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.rest.ApiApplication;
import com.grpctrl.rest.ContextListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

/**
 * Responsible for running the system.
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;

    /**
     * @param configSupplier the supplier of system configuration properties
     */
    public Runner(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = new ObjectMapperSupplier();
        this.oAuth20ServiceSupplier = new OAuth20ServiceSupplier(this.configSupplier);
    }

    @Nonnull
    private ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Nonnull
    private ObjectMapperSupplier getObjectMapperSupplier() {
        return this.objectMapperSupplier;
    }

    @Nonnull
    private OAuth20ServiceSupplier getOAuth20ServiceSupplier() {
        return this.oAuth20ServiceSupplier;
    }

    /**
     * Run the application server.
     */
    public void run() {
        final Config config = getConfigSupplier().get();
        final String host = config.getString(ConfigKeys.SYSTEM_HOST.getKey());
        final int port = config.getInt(ConfigKeys.SYSTEM_PORT.getKey());
        final String webContent = config.getString(ConfigKeys.WEB_CONTENT.getKey());

        final Optional<URL> jaasLogin = Optional.ofNullable(Runner.class.getClassLoader().getResource("login.conf"));
        if (jaasLogin.isPresent()) {
            LOG.info("Using JAAS Configuration: {}", jaasLogin.get().getFile());
            System.setProperty("jaas.login.conf", jaasLogin.get().getFile());
            System.setProperty("java.security.auth.login.config", jaasLogin.get().getFile());
        }
        LOG.info("Web Content directory: {}", new File(webContent).getAbsolutePath());

        final Constraint accountConstraint = new Constraint();
        accountConstraint.setName("AccountConstraint");
        accountConstraint.setRoles(new String[] {"ADMIN"});
        accountConstraint.setAuthenticate(true);
        final ConstraintMapping accountConstraintMapping = new ConstraintMapping();
        accountConstraintMapping.setPathSpec("/api/account*");
        accountConstraintMapping.setConstraint(accountConstraint);

        final Constraint groupConstraint = new Constraint();
        groupConstraint.setName("AccountConstraint");
        groupConstraint.setRoles(new String[] {"USER"});
        groupConstraint.setAuthenticate(true);
        final ConstraintMapping groupConstraintMapping = new ConstraintMapping();
        groupConstraintMapping.setPathSpec("/api/group*");
        groupConstraintMapping.setConstraint(groupConstraint);

        final List<ConstraintMapping> constraintMappings = new ArrayList<>(2);
        constraintMappings.add(accountConstraintMapping);
        constraintMappings.add(groupConstraintMapping);

        final ConstraintSecurityHandler constraintSecurityHandler = new ConstraintSecurityHandler();
        constraintSecurityHandler.setConstraintMappings(constraintMappings, Sets.newHashSet("ADMIN", "USER"));
        constraintSecurityHandler.setLoginService(
                new CustomLoginService(getConfigSupplier(), getObjectMapperSupplier(), getOAuth20ServiceSupplier()));
        constraintSecurityHandler.setAuthenticator(new FormAuthenticator("/api/auth/login", "/", false));
        constraintSecurityHandler.setIdentityService(new DefaultIdentityService());

        final ServletContextHandler servletContextHandler =
                new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        servletContextHandler.addEventListener(new ContextListener());
        servletContextHandler.setContextPath("/");
        servletContextHandler.setSecurityHandler(constraintSecurityHandler);

        final Server server = new Server(new InetSocketAddress(host, port));
        server.setHandler(servletContextHandler);

        final ServletHolder jerseyServlet = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("javax.ws.rs.Application", ApiApplication.class.getName());

        final ServletHolder webServlet = servletContextHandler.addServlet(DefaultServlet.class, "/*");
        jerseyServlet.setInitOrder(2);
        webServlet.setInitParameter("resourceBase", webContent);
        webServlet.setInitParameter("dirAllowed", "true");
        webServlet.setInitParameter("pathInfoOnly", "true");

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
            httpConfiguration.setSecurePort(port);
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
            sslConnector.setPort(port);
            server.setConnectors(new Connector[] {sslConnector});
        }

        try {
            server.start();
            server.join();
        } catch (final Throwable throwable) {
            LOG.error("Problem with Jetty", throwable);
        }

        LOG.info("Server started on {}:{}", host, port);
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
