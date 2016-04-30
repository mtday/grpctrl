package com.grpctrl.common.config;

import static org.junit.Assert.assertNotNull;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Perform testing on the {@link ConfigSupplier}.
 */
public class ConfigSupplierTest {
    private static ConfigSupplier supplier;

    @BeforeClass
    public static void beforeClass() {
        supplier = new ConfigSupplier();
    }

    @Test
    public void testGet() {
        assertNotNull(supplier.get());
    }

    @Test
    public void testProvide() {
        assertNotNull(supplier.provide());
    }

    @Test
    public void testDispose() {
        // Nothing to really test here.
        supplier.dispose(supplier.get());
    }

    @Test
    public void testBinder() {
        // Nothing to really test here.
        new ConfigSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
