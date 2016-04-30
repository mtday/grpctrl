package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing on the {@link Member} class.
 */
public class MemberTest {
    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullId() {
        new Member(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTags() {
        new Member("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithTooLongId() {
        new Member(StringUtils.leftPad("", Member.Validator.MAX_ID_LENGTH + 1, "I"));
    }

    @Test
    public void testConstructorWithLongId() {
        final String id = StringUtils.leftPad("", Member.Validator.MAX_ID_LENGTH, "I");
        final Member member = new Member(id);

        assertEquals(id, member.getId());
    }

    @Test
    public void testCompareTo() {
        final Member a = new Member("id1");
        final Member b = new Member("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Member c = new Member("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

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
        final Member a = new Member("id1");
        final Member b = new Member("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Member c = new Member("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

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
        final Member a = new Member("id1");
        final Member b = new Member("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Member c = new Member("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

        assertEquals(-573476116, a.hashCode());
        assertEquals(-573152425, b.hashCode());
        assertEquals(-573152351, c.hashCode());
    }

    @Test
    public void testToString() {
        final Member member = new Member("id", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        assertEquals(
                "Member[memberType=INDIVIDUAL,id=id,tags=[Tag[label=t1,value=v1], Tag[label=t1,value=v2]]]",
                member.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Member.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testValidatorWithNullId() {
        Member.Validator.validateId(null);
    }
}
