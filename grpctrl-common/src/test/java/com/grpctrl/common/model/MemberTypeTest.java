package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link MemberType} class.
 */
public class MemberTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertTrue(MemberType.values().length > 0);
        assertEquals(MemberType.INDIVIDUAL, MemberType.valueOf(MemberType.INDIVIDUAL.name()));
    }
}
