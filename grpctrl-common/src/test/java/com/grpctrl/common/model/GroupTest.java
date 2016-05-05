package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Perform testing on the {@link Group} class.
 */
public class GroupTest {
    @Test
    public void testCompareTo() {
        final Group a = new Group("name1");
        final Group b = new Group(1L, "name1");
        final Group c = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t1", "v2"));
        final Group d = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));
        final Group e = new Group("name2").setId(1L).setParentId(2L)
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(-1, a.compareTo(e));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(1, b.compareTo(d));
        assertEquals(-1, b.compareTo(e));
        assertEquals(1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(-1, c.compareTo(e));
        assertEquals(1, d.compareTo(a));
        assertEquals(-1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
        assertEquals(-1, d.compareTo(e));
        assertEquals(1, e.compareTo(a));
        assertEquals(1, e.compareTo(b));
        assertEquals(1, e.compareTo(c));
        assertEquals(1, e.compareTo(d));
        assertEquals(0, e.compareTo(e));
    }

    @Test
    public void testEquals() {
        final Group a = new Group("name1");
        final Group b = new Group("name1").setId(1L);
        final Group c = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t1", "v2"));
        final Group d = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));
        final Group e = new Group("name2").setId(1L).setParentId(2L)
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(b, d);
        assertNotEquals(b, e);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
        assertNotEquals(c, d);
        assertNotEquals(c, e);
        assertNotEquals(d, a);
        assertNotEquals(d, b);
        assertNotEquals(d, c);
        assertEquals(d, d);
        assertNotEquals(d, e);
        assertNotEquals(e, a);
        assertNotEquals(e, b);
        assertNotEquals(e, c);
        assertNotEquals(e, d);
        assertEquals(e, e);
    }

    @Test
    public void testHashCode() {
        final Group a = new Group("name1");
        final Group b = new Group("name1").setId(1L);
        final Group c = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t1", "v2"));
        final Group d = new Group("name1")
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));
        final Group e = new Group("name2").setId(1L).setParentId(2L)
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));

        assertEquals(-393462817, a.hashCode());
        assertEquals(-393412164, b.hashCode());
        assertEquals(-393139126, c.hashCode());
        assertEquals(-393139089, d.hashCode());
        assertEquals(-393085661, e.hashCode());
    }

    @Test
    public void testToString() {
        final Group group = new Group("name").setId(1L).setParentId(2L)
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));
        assertEquals("Group[id=Optional[1],parentId=Optional[2],name=name,tags=[Tag[label=t1,value=v1], Tag[label=t2,"
                + "value=v2]]]", group.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Group.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testWithNullName() {
        new Group((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTooLongName() {
        new Group(StringUtils.leftPad("", Group.Validator.MAX_NAME_LENGTH + 1, "N"));
    }

    @Test
    public void testWithLongName() {
        final String name = StringUtils.leftPad("", Group.Validator.MAX_NAME_LENGTH, "N");
        final Group group = new Group(name);

        assertEquals(name, group.getName());
    }

    @Test
    public void testCopy() {
        final Group group = new Group("g").setId(1L).setParentId(2L)
                .addTags(new Tag("t1", "v1"), new Tag("t2", "v2"));

        assertEquals(group, new Group(group));
    }

    @Test
    public void testDefaultConstructor() {
        final Group group = new Group();

        assertFalse(group.getId().isPresent());
        assertFalse(group.getParentId().isPresent());
        assertEquals("group-name", group.getName());
        assertTrue(group.getTags().isEmpty());
    }

    @Test
    public void testParameterConstructor() {
        final Group group = new Group(1L, null, "name", Sets.newHashSet(new Tag("a", "a"), new Tag("b", "b")));

        assertEquals(new Long(1), group.getId().orElse(null));
        assertFalse(group.getParentId().isPresent());
        assertEquals("name", group.getName());
        assertEquals(2, group.getTags().size());
        assertTrue(group.getTags().containsAll(Sets.newHashSet(new Tag("a", "a"), new Tag("b", "b"))));
    }

    @Test
    public void testTags() {
        final Group group = new Group().addTags(Sets.newHashSet(new Tag("zzz", "zzz")));
        group.setTags(new Tag("a", "a"), new Tag("b", "b"));

        assertEquals(2, group.getTags().size());
        assertTrue(group.getTags().containsAll(Sets.newHashSet(new Tag("a", "a"), new Tag("b", "b"))));
    }
}
