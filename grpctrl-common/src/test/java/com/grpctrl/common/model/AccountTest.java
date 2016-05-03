package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Perform testing on the {@link Account} class.
 */
public class AccountTest {
    @Test
    public void testHasId() {
        final Account a = new Account.Builder("name1", new ServiceLevel.Builder().build()).build();
        final Account b = new Account.Builder("name1", new ServiceLevel.Builder().build()).setId(1L).build();

        assertFalse(a.hasId());
        assertTrue(b.hasId());
    }

    @Test
    public void testCompareTo() {
        final Account a = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build()).build();
        final Account b = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build()).build();
        final Account c = new Account.Builder(10L, "name2", new ServiceLevel.Builder().build()).build();

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
        final Account a = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build()).build();
        final Account b = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build()).build();
        final Account c = new Account.Builder(10L, "name2", new ServiceLevel.Builder().build()).build();

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
        final Account a = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxDepth(1).build()).build();
        final Account b = new Account.Builder("name1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build()).build();
        final Account c = new Account.Builder(10L, "name2", new ServiceLevel.Builder().build()).build();

        assertEquals(-423599945, a.hashCode());
        assertEquals(-423599906, b.hashCode());
        assertEquals(-423413722, c.hashCode());
    }

    @Test
    public void testToString() {
        final Account a = new Account.Builder("name",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build()).build();
        final Account b = new Account.Builder(10L, "name",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxDepth(3).build()).build();
        assertEquals("Account[id=Optional.empty,name=name,serviceLevel=ServiceLevel[maxGroups=1,maxTags=2,maxDepth=3]]",
                a.toString());
        assertEquals("Account[id=Optional[10],name=name,serviceLevel=ServiceLevel[maxGroups=1,maxTags=2,maxDepth=3]]",
                b.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Account.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testValidatorWithNullName() {
        Account.Validator.validateName(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullId() {
        new Account.Builder(null, new ServiceLevel.Builder().build());
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullServiceLevel() {
        new Account.Builder("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongName() {
        new Account.Builder(
                StringUtils.leftPad("", Account.Validator.MAX_NAME_LENGTH + 1, "N"), new ServiceLevel.Builder().build())
                .build();
    }

    @Test
    public void testBuilderWithLongName() {
        final String name = StringUtils.leftPad("", Account.Validator.MAX_NAME_LENGTH, "N");
        final Account account = new Account.Builder(name, new ServiceLevel.Builder().build()).build();

        assertEquals(name, account.getName());
    }

    @Test
    public void testBuilderCopy() {
        final Account original = new Account.Builder("name", new ServiceLevel.Builder().build()).build();
        final Account copy = new Account.Builder(original).build();

        assertEquals(original, copy);
    }

    @Test
    public void testBuilderClearId() {
        assertFalse(
                new Account.Builder("name", new ServiceLevel.Builder().build()).setId(1L).clearId().build().hasId());
    }
}
