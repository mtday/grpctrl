package com.grpctrl.common.supplier;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

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
 * Provides singleton access to a {@link MetricRegistry} for managing system metrics.
 */
@Provider
public class MetricRegistrySupplier implements Supplier<MetricRegistry>, Factory<MetricRegistry>,
        ContextResolver<MetricRegistry> {
    @Nonnull
    private final MetricRegistry singleton;

    /**
     * @param servletContext the servlet context environment into which the registry will be stored.
     */
    @Inject
    public MetricRegistrySupplier(@Nonnull final ServletContext servletContext) {
        // Store the registry in the servlet context so it can be accessed by the MetricsServlet.
        this.singleton =
                (MetricRegistry) Objects.requireNonNull(servletContext).getAttribute(MetricsServlet.METRICS_REGISTRY);

        if (this.singleton == null) {
            throw new RuntimeException("Servlet context did not provide a metric registry, verify that the context "
                    + "listener is defined in the web.xml for this service and that it sets the metric registry.");
        }
    }

    @Override
    @Nonnull
    public MetricRegistry get() {
        return this.singleton;
    }

    @Override
    @Nonnull
    public MetricRegistry getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public MetricRegistry provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final MetricRegistry metricRegistry) {
        // Nothing to do.
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(MetricRegistrySupplier.class).to(MetricRegistrySupplier.class).in(Singleton.class);
        }
    }
}
