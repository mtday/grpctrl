package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link ServiceType} class.
 */
public class ServiceTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(ServiceType.values().length > 0);
        assertEquals(ServiceType.MEMORY, ServiceType.valueOf(ServiceType.MEMORY.name()));
    }
}
