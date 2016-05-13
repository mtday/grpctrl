package com.grpctrl.run;

import com.google.common.collect.Sets;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.rest.ApiApplication;
import com.grpctrl.rest.ContextListener;
import com.grpctrl.security.CustomLoginServiceSupplier;
import com.typesafe.config.Config;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Responsible for running the system.
 */
public class Runner {
    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    @Nonnull
    private final InjectionManager injectionManager;

    /**
     * @param injectionManager the manager responsible for tracking dependency injection objects
     */
    public Runner(@Nonnull final InjectionManager injectionManager) {
        this.injectionManager = Objects.requireNonNull(injectionManager);
    }

    /**
     * Run the application server.
     */
    public void run() {
        final Server server = getServer();
        try {
            server.start();
            server.join();
            LOG.info("Server started");
        } catch (final Throwable throwable) {
            LOG.error("Problem with Jetty", throwable);
        }
    }

    private Server getServer() {
        final Config config = this.injectionManager.get(ConfigSupplier.class).get();
        final String host = config.getString(ConfigKeys.SYSTEM_HOST.getKey());
        final int port = config.getInt(ConfigKeys.SYSTEM_PORT.getKey());

        final Server server = new Server(new InetSocketAddress(host, port));
        server.setHandler(getContextHandler());
        configureSsl(server);
        return server;
    }

    private ServletContextHandler getContextHandler() {
        final Config config = this.injectionManager.get(ConfigSupplier.class).get();
        final String webContent = config.getString(ConfigKeys.WEB_CONTENT.getKey());

        final SessionManager sessionManager = new HashSessionManager();
        sessionManager.setMaxInactiveInterval((int) TimeUnit.MINUTES.toSeconds(5));
        final SessionHandler sessionHandler = new SessionHandler(sessionManager);

        final ServletContextHandler servletContextHandler =
                new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        servletContextHandler.setSessionHandler(sessionHandler);
        servletContextHandler.addEventListener(new ContextListener());
        servletContextHandler.setContextPath("/");
        servletContextHandler.setSecurityHandler(getSecurityHandler());
        servletContextHandler
                .setAttribute(ServletProperties.SERVICE_LOCATOR, this.injectionManager.getServiceLocator());

        final ServletHolder jerseyServlet = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("javax.ws.rs.Application", ApiApplication.class.getName());

        final ServletHolder webServlet = servletContextHandler.addServlet(DefaultServlet.class, "/*");
        webServlet.setInitOrder(2);
        webServlet.setInitParameter("resourceBase", webContent);

        return servletContextHandler;
    }

    private ConstraintSecurityHandler getSecurityHandler() {
        final Constraint accountConstraint = new Constraint();
        accountConstraint.setName("AccountsConstraint");
        accountConstraint.setRoles(new String[] {"ADMIN"});
        accountConstraint.setAuthenticate(true);
        final ConstraintMapping accountConstraintMapping = new ConstraintMapping();
        accountConstraintMapping.setPathSpec("/api/account*");
        accountConstraintMapping.setConstraint(accountConstraint);

        final Constraint groupConstraint = new Constraint();
        groupConstraint.setName("GroupsConstraint");
        groupConstraint.setRoles(new String[] {"USER"});
        groupConstraint.setAuthenticate(true);
        final ConstraintMapping groupConstraintMapping = new ConstraintMapping();
        groupConstraintMapping.setPathSpec("/api/group*");
        groupConstraintMapping.setConstraint(groupConstraint);

        final List<ConstraintMapping> constraintMappings = new ArrayList<>(2);
        constraintMappings.add(accountConstraintMapping);
        constraintMappings.add(groupConstraintMapping);

        final Optional<URL> loginModule = Optional.ofNullable(Runner.class.getClassLoader().getResource("login.conf"));
        if (loginModule.isPresent()) {
            // TODO: Are these necessary?
            System.setProperty("jaas.login.conf", loginModule.get().getFile());
            System.setProperty("java.security.auth.login.config", loginModule.get().getFile());
        }

        final ConstraintSecurityHandler constraintSecurityHandler = new ConstraintSecurityHandler();
        constraintSecurityHandler.setConstraintMappings(constraintMappings, Sets.newHashSet("ADMIN", "USER"));
        constraintSecurityHandler.setLoginService(this.injectionManager.get(CustomLoginServiceSupplier.class).get());
        constraintSecurityHandler.setAuthenticator(new FormAuthenticator("/api/auth/login", "/", false));
        return constraintSecurityHandler;
    }

    private void configureSsl(@Nonnull final Server server) {
        final Config config = this.injectionManager.get(ConfigSupplier.class).get();
        if (config.getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
            final int port = config.getInt(ConfigKeys.SYSTEM_PORT.getKey());
            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(this.injectionManager.get(SslContextSupplier.class).get());

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
    }

    /**
     * The entry-point into running this system.
     *
     * @param args the command-line parameters
     */
    public static void main(final String... args) {
        new Runner(new InjectionManager()).run();
    }
}
