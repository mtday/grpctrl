package com.grpctrl.common.supplier;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to a {@link HealthCheckRegistry} for managing component health status.
 */
@Provider
public class HealthCheckRegistrySupplier
        implements Supplier<HealthCheckRegistry>, Factory<HealthCheckRegistry>, ContextResolver<HealthCheckRegistry> {
    @Nonnull
    private HealthCheckRegistry singleton;

    /**
     * @param servletContext the servlet context environment into which the registry will be stored.
     */
    @Inject
    public HealthCheckRegistrySupplier(@Nonnull final ServletContext servletContext) {
        // Store the registry in the servlet context so it can be accessed by the HealthCheckServlet.
        this.singleton = (HealthCheckRegistry) Objects.requireNonNull(servletContext)
                .getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY);

        if (this.singleton == null) {
            throw new RuntimeException("Servlet context did not provide a health check registry, verify that the "
                    + "context listener is defined in the web.xml for this service and that it sets the health check "
                    + "registry.");
        }
    }

    @Override
    @Nonnull
    public HealthCheckRegistry get() {
        return this.singleton;
    }

    @Override
    @Nonnull
    public HealthCheckRegistry getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public HealthCheckRegistry provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final HealthCheckRegistry healthCheckRegistry) {
        // Nothing to do.
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(HealthCheckRegistrySupplier.class).to(HealthCheckRegistrySupplier.class).in(Singleton.class);
        }
    }
}
