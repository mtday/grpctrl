package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Perform testing on the {@link Group} class.
 */
public class GroupTest {
    @Test
    public void testHasId() {
        final Group a = new Group.Builder("name1").build();
        final Group b = new Group.Builder("name1").setId(1L).build();

        assertFalse(a.hasId());
        assertTrue(b.hasId());
    }

    @Test
    public void testHasParentId() {
        final Group a = new Group.Builder("name1").build();
        final Group b = new Group.Builder("name1").setParentId(1L).build();

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
        final Group a = new Group.Builder("name1").build();
        final Group b = new Group.Builder("name1").setId(1L).build();
        final Group c = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group d = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build())
                .addChildBuilder(new Group.Builder("g2")).build();
        final Group e = new Group.Builder("name2").setId(1L).setParentId(2L)
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildBuilders(new Group.Builder("g1"),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build())).build();

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
        final Group a = new Group.Builder("name1").build();
        final Group b = new Group.Builder("name1").setId(1L).build();
        final Group c = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group d = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build())
                .addChildBuilder(new Group.Builder("g2")).build();
        final Group e = new Group.Builder("name2").setId(1L).setParentId(2L)
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildBuilders(new Group.Builder("g1"),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build())).build();

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
        final Group a = new Group.Builder("name1").build();
        final Group b = new Group.Builder("name1").setId(1L).build();
        final Group c = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build()).build();
        final Group d = new Group.Builder("name1")
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t1", "v2").build())
                .addChild(new Group.Builder("g1").build())
                .addChildBuilder(new Group.Builder("g2")).build();
        final Group e = new Group.Builder("name2").setId(1L).setParentId(2L)
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildBuilders(new Group.Builder("g1"),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build())).build();

        assertEquals(-1673222341, a.hashCode());
        assertEquals(-1671348180, b.hashCode());
        assertEquals(-1661245774, c.hashCode());
        assertEquals(705326729, d.hashCode());
        assertEquals(713293199, e.hashCode());
    }

    @Test
    public void testToString() {
        final Group group = new Group.Builder("name").setId(1L).setParentId(2L)
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();
        assertEquals("Group[id=Optional[1],parentId=Optional[2],name=name,tags=[Tag[label=t1,value=v1], Tag[label=t2,"
                + "value=v2]],children=[Group[id=Optional.empty,parentId=Optional.empty,name=g1,tags=[],"
                + "children=[]], Group[id=Optional.empty,parentId=Optional.empty,name=g2,tags=[Tag[label=t1,"
                + "value=v1]],children=[]]]]", group.toString());
    }

    @Test
    public void testContainsName() {
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

        assertTrue(g.containsName(gm.getName()));
        assertTrue(g.containsName(g111.getName()));
        assertTrue(g.containsName(g112.getName()));
        assertTrue(g.containsName(g11.getName()));
        assertTrue(g.containsName(g121.getName()));
        assertTrue(g.containsName(g122.getName()));
        assertTrue(g.containsName(g12.getName()));
        assertTrue(g.containsName(g1.getName()));
        assertTrue(g.containsName(g2.getName()));
        assertTrue(g.containsName(g.getName()));
        assertFalse(g.containsName("x"));
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Group.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullName() {
        new Group.Builder((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongName() {
        new Group.Builder(StringUtils.leftPad("", Group.Validator.MAX_NAME_LENGTH + 1, "N"));
    }

    @Test
    public void testBuilderWithLongName() {
        final String name = StringUtils.leftPad("", Group.Validator.MAX_NAME_LENGTH, "N");
        final Group group = new Group.Builder(name).build();

        assertEquals(name, group.getName());
    }

    @Test
    public void testBuilderCopy() {
        final Group group = new Group.Builder("g").setId(1L).setParentId(2L)
                .addTags(new Tag.Builder("t1", "v1").build(), new Tag.Builder("t2", "v2").build())
                .addChildren(new Group.Builder("g1").build(),
                        new Group.Builder("g2").addTag(new Tag.Builder("t1", "v1").build()).build()).build();

        assertEquals(group, new Group.Builder(group).build());
    }

    @Test
    public void testBuilderClearId() {
        final Group group = new Group.Builder("id").setId(1L).clearId().build();

        assertFalse(group.getId().isPresent());
    }

    @Test
    public void testBuilderClearParentId() {
        final Group group = new Group.Builder("id").setParentId(1L).clearParentId().build();

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
