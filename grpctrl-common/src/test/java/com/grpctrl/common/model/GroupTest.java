package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Perform testing on the {@link Group} class.
 */
public class GroupTest {
    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullId() {
        new Group(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTags() {
        new Group("id", null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullMembers() {
        new Group("id", Collections.singleton(new Tag("t", "v")), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithTooLongId() {
        new Group(StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH + 1, "I"));
    }

    @Test
    public void testConstructorWithLongId() {
        final String id = StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH, "I");
        final Group group = new Group(id);

        assertEquals(id, group.getId());
    }

    @Test
    public void testCompareTo() {
        final Group a = new Group("id1");
        final Group b = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Group c = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")),
                Collections.singleton(new Member("m1")));
        final Group d = new Group("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")),
                Arrays.asList(new Member("m1"), new Member("m2", Collections.singleton(new Tag("t1", "v1")))));
        final Member e = new Member("m1");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(1, a.compareTo(e));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-1, b.compareTo(d));
        assertEquals(1, b.compareTo(e));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(1, c.compareTo(e));
        assertEquals(1, d.compareTo(a));
        assertEquals(1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
        assertEquals(1, d.compareTo(e));
    }

    @Test
    public void testEquals() {
        final Group a = new Group("id1");
        final Group b = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Group c = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")),
                Collections.singleton(new Member("m1")));
        final Group d = new Group("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")),
                Arrays.asList(new Member("m1"), new Member("m2", Collections.singleton(new Tag("t1", "v1")))));

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(b, d);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
        assertNotEquals(c, d);
        assertNotEquals(d, a);
        assertNotEquals(d, b);
        assertNotEquals(d, c);
        assertEquals(d, d);
    }

    @Test
    public void testHashCode() {
        final Group a = new Group("id1");
        final Group b = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Group c = new Group("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")),
                Collections.singleton(new Member("m1")));
        final Group d = new Group("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")),
                Arrays.asList(new Member("m1"), new Member("m2", Collections.singleton(new Tag("t1", "v1")))));

        assertEquals(353686259, a.hashCode());
        assertEquals(365662826, b.hashCode());
        assertEquals(-211536452, c.hashCode());
        assertEquals(-788571110, d.hashCode());
    }

    @Test
    public void testToString() {
        final Group group = new Group("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")),
                Arrays.asList(new Member("m1"), new Member("m2", Collections.singleton(new Tag("t1", "v1")))));
        assertEquals(
                "Group[memberType=GROUP,id=id2,tags=[Tag[label=t1,value=v1], Tag[label=t2,value=v2]],"
                        + "members=[Member[memberType=INDIVIDUAL,id=m1,tags=[]], Member[memberType=INDIVIDUAL,id=m2,"
                        + "tags=[Tag[label=t1,value=v1]]]]]",
                group.toString());
    }
}
