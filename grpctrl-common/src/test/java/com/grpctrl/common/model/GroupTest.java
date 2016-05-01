package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

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
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build()).addChildren(
                        new Group.Builder("g1").build(),
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
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build()).addChildren(
                        new Group.Builder("g1").build(),
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
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build()).addChildren(
                        new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

        assertEquals(1007540703, a.hashCode());
        assertEquals(1019517270, b.hashCode());
        assertEquals(1215595033, c.hashCode());
        assertEquals(1417763736, d.hashCode());
    }

    @Test
    public void testToString() {
        final Group group = new Group.Builder("id2")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build()).addChildren(
                        new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();
        assertEquals(
                "Group[id=id2,parentId=Optional.empty,tags=[Tag[label=t1,value=v1], Tag[label=t2,value=v2]],"
                        + "children=[Group[id=g1,parentId=Optional.empty,tags=[],children=[]], Group[id=g2,"
                        + "parentId=Optional.empty,tags=[Tag[label=t1,value=v1]],children=[]]]]",
                group.toString());
    }

    @Test
    public void testContains() {
        final Group gm = new Group.Builder("m").build();
        final Group g111 = new Group.Builder("111").addChild(gm).build();
        final Group g112 = new Group.Builder("112").addChild(gm).build();
        final Group g11 = new Group.Builder("11").addChildren(g111, g112, gm).build();
        final Group g121 = new Group.Builder("121").build();
        final Group g122 = new Group.Builder("122").build();
        final Group g12 = new Group.Builder("12").addChildren(g121, g122, gm).build();
        final Group g1 = new Group.Builder("1").addChildren(g11, g12, gm).build();
        final Group g2 = new Group.Builder("2").build();
        final Group g = new Group.Builder("").addChildren(g1, g2).build();

        assertTrue(g.contains(gm.getId()));
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
        final Group gm = new Group.Builder("m").build();
        final Group g111 = new Group.Builder("111").addChild(gm).build();
        final Group g112 = new Group.Builder("112").addChild(gm).build();
        final Group g11 = new Group.Builder("11").addChildren(g111, g112, gm).build();
        final Group g121 = new Group.Builder("121").build();
        final Group g122 = new Group.Builder("122").build();
        final Group g12 = new Group.Builder("12").addChildren(g121, g122, gm).build();
        final Group g1 = new Group.Builder("1").addChildren(g11, g12, gm).build();
        final Group g2 = new Group.Builder("2").addChildren(new Group.Builder("2").addChild(gm).build()).build();

        final Iterator<Group> iter = Group.flatten(Arrays.asList(g1, g2)).iterator();

        Group group = iter.next();
        assertFalse(group.hasParentId());
        assertEquals("1", group.getId());

        group = iter.next();
        assertEquals("1", group.getParentId().orElse(null));
        assertEquals("11", group.getId());

        group = iter.next();
        assertEquals("11", group.getParentId().orElse(null));
        assertEquals("111", group.getId());

        group = iter.next();
        assertEquals("111", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        group = iter.next();
        assertEquals("11", group.getParentId().orElse(null));
        assertEquals("112", group.getId());

        group = iter.next();
        assertEquals("112", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        group = iter.next();
        assertEquals("11", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        group = iter.next();
        assertEquals("1", group.getParentId().orElse(null));
        assertEquals("12", group.getId());

        group = iter.next();
        assertEquals("12", group.getParentId().orElse(null));
        assertEquals("121", group.getId());

        group = iter.next();
        assertEquals("12", group.getParentId().orElse(null));
        assertEquals("122", group.getId());

        group = iter.next();
        assertEquals("12", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        group = iter.next();
        assertEquals("1", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        group = iter.next();
        assertFalse(group.getParentId().isPresent());
        assertEquals("2", group.getId());

        group = iter.next();
        assertEquals("2", group.getParentId().orElse(null));
        assertEquals("2", group.getId());

        group = iter.next();
        assertEquals("2", group.getParentId().orElse(null));
        assertEquals("m", group.getId());

        assertFalse(iter.hasNext());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Group.Validator();
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

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongParentId() {
        new Group.Builder("").setParentId(StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH + 1, "I"));
    }

    @Test
    public void testBuilderWithLongId() {
        final String id = StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH, "I");
        final Group group = new Group.Builder(id).build();

        assertEquals(id, group.getId());
    }

    @Test
    public void testBuilderWithLongParentId() {
        final String parentId = StringUtils.leftPad("", Group.Validator.MAX_ID_LENGTH, "I");
        final Group group = new Group.Builder("id").setParentId(parentId).build();

        assertEquals(parentId, group.getParentId().orElse(null));
    }

    @Test
    public void testBuilderCopy() {
        final Group group =
                new Group.Builder("g").addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                        .addChildren(
                                new Group.Builder("g1").build(),
                                new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

        assertEquals(group, new Group.Builder(group).build());
    }

    @Test
    public void testBuilderClearParentId() {
        final Group group = new Group.Builder("id").setParentId("p").clearParentId().build();

        assertFalse(group.getParentId().isPresent());
    }

    @Test
    public void testBuilderRemoveTag() {
        final Tag tag1 = new Tag.Builder("1", "v").build();
        final Tag tag2 = new Tag.Builder("2", "v").build();
        final Group group = new Group.Builder("id").addTags(tag1, tag2).removeTag(tag1).build();

        assertTrue(group.hasTags());
        assertEquals(1, group.getTags().size());
        assertTrue(group.getTags().contains(tag2));
    }

    @Test
    public void testBuilderClearTags() {
        final Tag tag1 = new Tag.Builder("1", "v").build();
        final Tag tag2 = new Tag.Builder("2", "v").build();
        final Group group = new Group.Builder("id").addTags(tag1, tag2).clearTags().build();

        assertFalse(group.hasTags());
    }

    @Test
    public void testBuilderDedupesTags() {
        final Tag tag1 = new Tag.Builder("L", "V").build();
        final Tag tag2 = new Tag.Builder("L", "V").build();
        final Group group = new Group.Builder("id").addTags(tag1, tag2).build();

        assertTrue(group.hasTags());
        assertEquals(1, group.getTags().size());
        assertTrue(group.getTags().contains(tag1));
        assertTrue(group.getTags().contains(tag2));
    }

    @Test
    public void testBuilderRemoveChild() {
        final Group grp1 = new Group.Builder("1").build();
        final Group grp2 = new Group.Builder("2").build();
        final Group group = new Group.Builder("id").addChildren(grp1, grp2).removeChild(grp1).build();

        assertTrue(group.hasChildren());
        assertEquals(1, group.getChildren().size());
        assertTrue(group.getChildren().contains(grp2));
    }

    @Test
    public void testBuilderClearChildren() {
        final Group grp1 = new Group.Builder("1").build();
        final Group grp2 = new Group.Builder("2").build();
        final Group group = new Group.Builder("id").addChildren(grp1, grp2).clearChildren().build();

        assertFalse(group.hasChildren());
    }

    @Test
    public void testBuilderDedupesChildren() {
        final Group grp1 = new Group.Builder("G").build();
        final Group grp2 = new Group.Builder("G").build();
        final Group group = new Group.Builder("id").addChildren(grp1, grp2).build();

        assertTrue(group.hasChildren());
        assertEquals(1, group.getChildren().size());
        assertTrue(group.getChildren().contains(grp1));
        assertTrue(group.getChildren().contains(grp2));
    }
}
