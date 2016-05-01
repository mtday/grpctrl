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
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxChildren(4).setMaxDepth(5).build();

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
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxChildren(4).setMaxDepth(5).build();

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
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build();
        final ServiceLevel b =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(2).build();
        final ServiceLevel c =
                new ServiceLevel.Builder().setMaxGroups(2).setMaxTags(3).setMaxChildren(4).setMaxDepth(5).build();

        assertEquals(31912797, a.hashCode());
        assertEquals(31912798, b.hashCode());
        assertEquals(31966303, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceLevel serviceLevel =
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxChildren(3).setMaxDepth(4).build();
        assertEquals("ServiceLevel[maxGroups=1,maxTags=2,maxChildren=3,maxDepth=4]", serviceLevel.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new ServiceLevel.Validator();
    }
}
