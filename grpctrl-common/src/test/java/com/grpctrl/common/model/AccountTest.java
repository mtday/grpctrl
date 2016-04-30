package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing on the {@link Account} class.
 */
public class AccountTest {
    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullId() {
        new Account(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTags() {
        new Account("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithTooLongId() {
        new Account(StringUtils.leftPad("", Account.Validator.MAX_ID_LENGTH + 1, "I"));
    }

    @Test
    public void testConstructorWithLongId() {
        final String id = StringUtils.leftPad("", Account.Validator.MAX_ID_LENGTH, "I");
        final Account account = new Account(id);

        assertEquals(id, account.getId());
    }

    @Test
    public void testCompareTo() {
        final Account a = new Account("id1");
        final Account b = new Account("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Account c = new Account("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

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
        final Account a = new Account("id1");
        final Account b = new Account("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Account c = new Account("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

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
        final Account a = new Account("id1");
        final Account b = new Account("id1", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        final Account c = new Account("id2", Arrays.asList(new Tag("t1", "v1"), new Tag("t2", "v2")));

        assertEquals(3873271, a.hashCode());
        assertEquals(4196962, b.hashCode());
        assertEquals(4197036, c.hashCode());
    }

    @Test
    public void testToString() {
        final Account account = new Account("id", Arrays.asList(new Tag("t1", "v1"), new Tag("t1", "v2")));
        assertEquals("Account[id=id,tags=[Tag[label=t1,value=v1], Tag[label=t1,value=v2]]]", account.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Account.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testValidatorWithNullId() {
        Account.Validator.validateId(null);
    }
}
