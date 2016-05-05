package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Perform testing on the {@link ServiceLevel} class.
 */
public class ServiceLevelTest {
    @Test
    public void testCompareTo() {
        final ServiceLevel a = new ServiceLevel(1, 1, 1);
        final ServiceLevel b = new ServiceLevel(1, 1, 2);
        final ServiceLevel c = new ServiceLevel(2, 3, 4);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ServiceLevel a = new ServiceLevel(1, 1, 1);
        final ServiceLevel b = new ServiceLevel(1, 1, 2);
        final ServiceLevel c = new ServiceLevel(2, 3, 4);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testHashCode() {
        final ServiceLevel a = new ServiceLevel(1, 1, 1);
        final ServiceLevel b = new ServiceLevel(1, 1, 2);
        final ServiceLevel c = new ServiceLevel(2, 3, 4);

        assertEquals(862508, a.hashCode());
        assertEquals(862509, b.hashCode());
        assertEquals(863954, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceLevel serviceLevel = new ServiceLevel(1, 2, 3);
        assertEquals("ServiceLevel[maxGroups=1,maxTags=2,maxDepth=3]", serviceLevel.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new ServiceLevel.Validator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatorNegativeMaxGroups() {
        ServiceLevel.Validator.validateMaxGroups(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatorNegativeMaxTags() {
        ServiceLevel.Validator.validateMaxTags(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatorNegativeMaxDepth() {
        ServiceLevel.Validator.validateMaxDepth(-1);
    }

    @Test
    public void tesCopy() {
        final ServiceLevel original = new ServiceLevel(2, 3, 4);
        final ServiceLevel copy = new ServiceLevel(original);

        assertEquals(original, copy);
    }
}
