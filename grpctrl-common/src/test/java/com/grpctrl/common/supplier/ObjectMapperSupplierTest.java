package com.grpctrl.common.supplier;

import static org.junit.Assert.assertNotNull;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Perform testing on the {@link ObjectMapperSupplier}.
 */
public class ObjectMapperSupplierTest {
    private static ObjectMapperSupplier supplier = null;

    @BeforeClass
    public static void beforeClass() {
        supplier = new ObjectMapperSupplier();
    }

    @Test
    public void testGet() {
        assertNotNull(supplier.get());
    }

    @Test
    public void testGetContext() {
        assertNotNull(supplier.getContext(getClass()));
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
        new ObjectMapperSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
