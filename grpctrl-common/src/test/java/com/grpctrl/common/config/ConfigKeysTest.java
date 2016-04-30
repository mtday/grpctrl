package com.grpctrl.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link ConfigKeys} class.
 */
public class ConfigKeysTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(ConfigKeys.values().length > 0);
        assertEquals(ConfigKeys.SYSTEM_NAME, ConfigKeys.valueOf(ConfigKeys.SYSTEM_NAME.name()));
    }

    @Test
    public void testGetKey() {
        assertEquals("system.name", ConfigKeys.SYSTEM_NAME.getKey());
    }
}
