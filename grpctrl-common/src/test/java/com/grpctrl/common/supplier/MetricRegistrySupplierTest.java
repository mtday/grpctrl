package com.grpctrl.common.supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;

/**
 * Perform testing on the {@link MetricRegistrySupplier}.
 */
public class MetricRegistrySupplierTest {
    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    private static MetricRegistrySupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY)).thenReturn(METRIC_REGISTRY);

        supplier = new MetricRegistrySupplier(servletContext);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingAttribute() {
        new MetricRegistrySupplier(Mockito.mock(ServletContext.class));
    }

    @Test
    public void testGet() {
        assertEquals(METRIC_REGISTRY, supplier.get());
    }

    @Test
    public void testGetContext() {
        assertEquals(METRIC_REGISTRY, supplier.getContext(getClass()));
    }

    @Test
    public void testProvide() {
        assertEquals(METRIC_REGISTRY, supplier.provide());
    }

    @Test
    public void testDispose() {
        // Nothing to really test here.
        supplier.dispose(supplier.get());
    }

    @Test
    public void testBinder() {
        // Nothing to really test here.
        new MetricRegistrySupplier.Binder().bind(mock(DynamicConfiguration.class));
    }
}
