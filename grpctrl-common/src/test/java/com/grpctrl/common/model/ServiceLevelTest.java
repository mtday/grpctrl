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
        final ServiceLevel a =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxDepth(4).build();

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
        final ServiceLevel a =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxDepth(4).build();

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
        final ServiceLevel a =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxDepth(4).build();

        assertEquals(862508, a.hashCode());
        assertEquals(862509, b.hashCode());
        assertEquals(863954, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceLevel serviceLevel =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build();
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
    public void testBuilderCopy() {
        final ServiceLevel original =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxDepth(4).build();
        final ServiceLevel copy = new ServiceLevel.Builder(original).build();

        assertEquals(original, copy);
    }
}
