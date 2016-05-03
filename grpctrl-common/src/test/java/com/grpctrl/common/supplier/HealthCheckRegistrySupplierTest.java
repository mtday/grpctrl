package com.grpctrl.common.supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;

/**
 * Perform testing on the {@link HealthCheckRegistrySupplier}.
 */
public class HealthCheckRegistrySupplierTest {
    private static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

    private static HealthCheckRegistrySupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
                .thenReturn(HEALTH_CHECK_REGISTRY);

        supplier = new HealthCheckRegistrySupplier(servletContext);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingAttribute() {
        new HealthCheckRegistrySupplier(Mockito.mock(ServletContext.class));
    }

    @Test
    public void testGet() {
        assertEquals(HEALTH_CHECK_REGISTRY, supplier.get());
    }

    @Test
    public void testGetContext() {
        assertEquals(HEALTH_CHECK_REGISTRY, supplier.getContext(getClass()));
    }

    @Test
    public void testProvide() {
        assertEquals(HEALTH_CHECK_REGISTRY, supplier.provide());
    }

    @Test
    public void testDispose() {
        // Nothing to really test here.
        supplier.dispose(supplier.get());
    }

    @Test
    public void testBinder() {
        // Nothing to really test here.
        new HealthCheckRegistrySupplier.Binder().bind(mock(DynamicConfiguration.class));
    }
}
