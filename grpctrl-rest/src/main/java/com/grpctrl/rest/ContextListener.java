package com.grpctrl.rest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Provides the servlet context listener for services.
 */
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(@Nonnull final ServletContextEvent event) {
        // Create the metric and health check registry so that the servlets will have them when they need them.
        // These values are also pulled from the servlet context by the corresponding suppliers.
        event.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, new MetricRegistry());
        event.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
    }

    @Override
    public void contextDestroyed(@Nonnull final ServletContextEvent event) {
        // Nothing to do.
    }
}

