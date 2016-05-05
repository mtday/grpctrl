package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link EndPoint} class.
 */
public class EndPointTest {
    @Test
    public void testDefaul() {
        final EndPoint ept = new EndPoint();
        assertEquals("localhost", ept.getHost());
        assertEquals(5000, ept.getPort());
        assertTrue(ept.isSecure());
    }

    @Test
    public void testAsUrl() {
        assertEquals("http://host1:1111/", new EndPoint("host1", 1111, false).asUrl());
        assertEquals("https://host1:1111/", new EndPoint("host1", 1111, true).asUrl());
        assertEquals("http://host1:2222/", new EndPoint("host1", 2222, false).asUrl());
        assertEquals("http://host2:1111/", new EndPoint("host2", 1111, false).asUrl());
    }

    @Test
    public void testCompareTo() {
        final EndPoint ept1 = new EndPoint("host1", 1111, false);
        final EndPoint ept2 = new EndPoint("host1", 1111, true);
        final EndPoint ept3 = new EndPoint("host1", 2222, false);
        final EndPoint ept4 = new EndPoint("host2", 1111, false);

        assertEquals(1, ept1.compareTo(null));

        assertEquals(0, ept1.compareTo(ept1));
        assertEquals(-1, ept1.compareTo(ept2));
        assertEquals(-1, ept1.compareTo(ept3));
        assertEquals(-1, ept1.compareTo(ept4));

        assertEquals(1, ept2.compareTo(ept1));
        assertEquals(0, ept2.compareTo(ept2));
        assertEquals(-1, ept2.compareTo(ept3));
        assertEquals(-1, ept2.compareTo(ept4));

        assertEquals(1, ept3.compareTo(ept1));
        assertEquals(1, ept3.compareTo(ept2));
        assertEquals(0, ept3.compareTo(ept3));
        assertEquals(-1, ept3.compareTo(ept4));

        assertEquals(1, ept4.compareTo(ept1));
        assertEquals(1, ept4.compareTo(ept2));
        assertEquals(1, ept4.compareTo(ept3));
        assertEquals(0, ept4.compareTo(ept4));
    }

    @Test
    public void testEquals() {
        final EndPoint ept1 = new EndPoint("host1", 1111, false);
        final EndPoint ept2 = new EndPoint("host1", 1111, true);
        final EndPoint ept3 = new EndPoint("host1", 2222, false);
        final EndPoint ept4 = new EndPoint("host2", 1111, false);

        assertNotEquals(ept1, null);

        assertEquals(ept1, ept1);
        assertNotEquals(ept1, ept2);
        assertNotEquals(ept1, ept3);
        assertNotEquals(ept1, ept4);

        assertNotEquals(ept2, ept1);
        assertEquals(ept2, ept2);
        assertNotEquals(ept2, ept3);
        assertNotEquals(ept2, ept4);

        assertNotEquals(ept3, ept1);
        assertNotEquals(ept3, ept2);
        assertEquals(ept3, ept3);
        assertNotEquals(ept3, ept4);

        assertNotEquals(ept4, ept1);
        assertNotEquals(ept4, ept2);
        assertNotEquals(ept4, ept3);
        assertEquals(ept4, ept4);
    }

    @Test
    public void testHashCode() {
        final EndPoint ept1 = new EndPoint("host1", 1111, false);
        final EndPoint ept2 = new EndPoint("host1", 1111, true);
        final EndPoint ept3 = new EndPoint("host1", 2222, false);
        final EndPoint ept4 = new EndPoint("host2", 1111, false);

        assertEquals(-1267529758, ept1.hashCode());
        assertEquals(-1267529759, ept2.hashCode());
        assertEquals(-1267488651, ept3.hashCode());
        assertEquals(-1267528389, ept4.hashCode());
    }

    @Test
    public void testToString() {
        final EndPoint ept = new EndPoint("host", 1111, false);
        assertEquals("EndPoint[host=host,port=1111,secure=false]", ept.toString());
    }

    @Test
    public void testCopy() {
        final EndPoint original = new EndPoint("host", 1111, false);
        final EndPoint copy = new EndPoint(original);
        assertEquals(original, copy);
    }
}

