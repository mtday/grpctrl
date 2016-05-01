package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Perform testing on the {@link Group} class.
 */
public class GroupTest {
    @Test
    public void testHasParentId() {
        final Group a = new Group.Builder("id1").build();
        final Group b = new Group.Builder("id1").setParentId("p").build();

        assertFalse(a.hasParentId());
        assertTrue(b.hasParentId());
    }

    @Test
    public void testHasTags() {
        final Group a = new Group.Builder("id1").build();
        final Group b = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();

        assertFalse(a.hasTags());
        assertTrue(b.hasTags());
    }

    @Test
    public void testHasChildren() {
        final Group a = new Group.Builder("id1").build();
        final Group b =
                new Group.Builder("id1").addChildren(new Group.Builder("g1").build(), new Group.Builder("g2").build())
                        .build();

        assertFalse(a.hasChildren());
        assertTrue(b.hasChildren());
    }

    @Test
    public void testCompareTo() {
        final Group a = new Group.Builder("id1").build();
        final Group b = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group c = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build()).build();
        final Group d = new Group.Builder("id2")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-1, b.compareTo(d));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(1, d.compareTo(a));
        assertEquals(1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final Group a = new Group.Builder("id1").build();
        final Group b = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group c = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build()).build();
        final Group d = new Group.Builder("id2")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

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
        final Group a = new Group.Builder("id1").build();
        final Group b = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group c = new Group.Builder("id1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build()).build();
        final Group d = new Group.Builder("id2")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

        assertEquals(1007540703, a.hashCode());
        assertEquals(1019517270, b.hashCode());
        assertEquals(1215595033, c.hashCode());
        assertEquals(1417763736, d.hashCode());
    }

    @Test
    public void testToString() {
        final Group group = new Group.Builder("id2")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();
        assertEquals(
                "Group[id=id2,parentId=Optional.empty,tags=[Tag[label=t1,value=v1], Tag[label=t2,value=v2]],"
                        + "children=[Group[id=g2,parentId=Optional.empty,tags=[Tag[label=t1,value=v1]],children=[]], "
                        + "Group[id=g1,parentId=Optional.empty,tags=[],children=[]]]]",
                group.toString());
    }

    @Test
    public void testContains() {
        final Group g1m = new Group.Builder("1m").build();
        final Group g111 = new Group.Builder("111").addChild(g1m).build();
        final Group g112 = new Group.Builder("112").addChild(g1m).build();
        final Group g11 = new Group.Builder("11").addChildren(g111, g112, g1m).build();
        final Group g121 = new Group.Builder("121").build();
        final Group g122 = new Group.Builder("122").build();
        final Group g12 = new Group.Builder("12").addChildren(g121, g122, g1m).build();
        final Group g1 = new Group.Builder("1").addChildren(g11, g12, g1m).build();
        final Group g2 = new Group.Builder("2").build();
        final Group g = new Group.Builder("").addChildren(g1, g2).build();

        assertTrue(g.contains(g1m.getId()));
        assertTrue(g.contains(g111.getId()));
        assertTrue(g.contains(g112.getId()));
        assertTrue(g.contains(g11.getId()));
        assertTrue(g.contains(g121.getId()));
        assertTrue(g.contains(g122.getId()));
        assertTrue(g.contains(g12.getId()));
        assertTrue(g.contains(g1.getId()));
        assertTrue(g.contains(g2.getId()));
        assertTrue(g.contains(g.getId()));
        assertFalse(g.contains("x"));
    }

    @Test
    public void testFlatten() {
        final Group g1m = new Group.Builder("1m").build();
        final Group g111 = new Group.Builder("111").addChild(g1m).build();
        final Group g112 = new Group.Builder("112").addChild(g1m).build();
        final Group g11 = new Group.Builder("11").addChildren(g111, g112, g1m).build();
        final Group g121 = new Group.Builder("121").build();
        final Group g122 = new Group.Builder("122").build();
        final Group g12 = new Group.Builder("12").addChildren(g121, g122, g1m).build();
        final Group g1 = new Group.Builder("1").addChildren(g11, g12, g1m).build();
        final Group g2 = new Group.Builder("2").build();

        final Map<ParentId, Collection<Group>> flattened = new TreeMap<>();
        flattened.putAll(Group.flatten(Arrays.asList(g1, g2)));

        final Iterator<Map.Entry<ParentId, Collection<Group>>> iter = flattened.entrySet().iterator();

        // These are the top-level groups that have no parent id.
        Map.Entry<ParentId, Collection<Group>> entry = iter.next();
        assertFalse(entry.getKey().hasId());
        assertEquals(2, entry.getValue().size());
        assertTrue(entry.getValue().contains(g1));
        assertTrue(entry.getValue().contains(g2));

        // Next is the parent id with value: 1
        entry = iter.next();
        assertEquals(new ParentId("1"), entry.getKey());
        assertEquals(3, entry.getValue().size());
        assertTrue(entry.getValue().contains(g11));
        assertTrue(entry.getValue().contains(g12));
        assertTrue(entry.getValue().contains(g1m));

        // Next is the parent id with value: 11
        entry = iter.next();
        assertEquals(new ParentId("11"), entry.getKey());
        assertEquals(3, entry.getValue().size());
        assertTrue(entry.getValue().contains(g111));
        assertTrue(entry.getValue().contains(g112));
        assertTrue(entry.getValue().contains(g1m));

        // Next is the parent id with value: 111
        entry = iter.next();
        assertEquals(new ParentId("111"), entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertTrue(entry.getValue().contains(g1m));

        // Next is the parent id with value: 112
        entry = iter.next();
        assertEquals(new ParentId("112"), entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertTrue(entry.getValue().contains(g1m));

        // Last is the parent id with value: 12
        entry = iter.next();
        assertEquals(new ParentId("12"), entry.getKey());
        assertEquals(3, entry.getValue().size());
        assertTrue(entry.getValue().contains(g121));
        assertTrue(entry.getValue().contains(g122));
        assertTrue(entry.getValue().contains(g1m));

        assertFalse(iter.hasNext());
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullId() {
        new Group.Builder((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongId() {
        new Group.Builder(StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH + 1, "I"));
    }

    @Test
    public void testBuilderWithLongId() {
        final String id = StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH, "I");
        final Group group = new Group.Builder(id).build();

        assertEquals(id, group.getId());
    }
}
