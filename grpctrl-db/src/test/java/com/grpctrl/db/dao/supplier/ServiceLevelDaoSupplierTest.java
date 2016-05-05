package com.grpctrl.db.dao.supplier;

import static org.junit.Assert.assertNotNull;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Perform testing on the {@link ServiceLevelDaoSupplier}.
 */
public class ServiceLevelDaoSupplierTest {
    private static ServiceLevelDaoSupplier supplier;

    @BeforeClass
    public static void beforeClass() {
        supplier = new ServiceLevelDaoSupplier();
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
        new ServiceLevelDaoSupplier.Binder().bind(Mockito.mock(DynamicConfiguration.class));
    }
}
