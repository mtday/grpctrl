package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Perform testing on the {@link ParentId} class.
 */
public class ParentIdTest {
    @Test
    public void testConstructorWithNullId() {
        final ParentId parent = new ParentId(null);
        assertFalse(parent.hasId());
        assertFalse(parent.getId().isPresent());
    }

    @Test
    public void testCompareTo() {
        final ParentId a = new ParentId();
        final ParentId b = new ParentId("id1");
        final ParentId c = new ParentId("id2");

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
        final ParentId a = new ParentId();
        final ParentId b = new ParentId("id1");
        final ParentId c = new ParentId("id2");

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
        final ParentId a = new ParentId();
        final ParentId b = new ParentId("id1");
        final ParentId c = new ParentId("id2");

        assertEquals(629, a.hashCode());
        assertEquals(104683, b.hashCode());
        assertEquals(104684, c.hashCode());
    }

    @Test
    public void testToString() {
        final ParentId a = new ParentId();
        final ParentId b = new ParentId("id");
        assertEquals("ParentId[id=Optional.empty]", a.toString());
        assertEquals("ParentId[id=Optional[id]]", b.toString());
    }
}
