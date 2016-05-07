package com.grpctrl.rest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Perform testing on the {@link ContextListener} class.
 */
public class ContextListenerTest {
    @Test
    public void testContextInitialized() {
        final ServletContextEvent event = Mockito.mock(ServletContextEvent.class);

        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(event.getServletContext()).thenReturn(servletContext);

        new ContextListener().contextInitialized(event);

        Mockito.verify(servletContext, Mockito.times(1))
                .setAttribute(Matchers.eq(MetricsServlet.METRICS_REGISTRY), Mockito.any(MetricRegistry.class));
        Mockito.verify(servletContext, Mockito.times(1))
                .setAttribute(Matchers.eq(HealthCheckServlet.HEALTH_CHECK_REGISTRY),
                        Mockito.any(HealthCheckRegistry.class));
    }

    @Test
    public void testContextDestroyed() {
        // Nothing to test really
        new ContextListener().contextDestroyed(Mockito.mock(ServletContextEvent.class));
    }
}

