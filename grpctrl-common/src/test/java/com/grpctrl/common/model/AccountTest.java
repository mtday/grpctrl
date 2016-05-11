package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Perform testing on the {@link Account} class.
 */
public class AccountTest {
    @Test
    public void testCompareTo() {
        final Account a = new Account("name1", new ServiceLevel(1, 1, 1));
        final Account b = new Account("name1");
        final Account c = new Account(10L, "name2", new ServiceLevel());

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
        final Account a = new Account("name1", new ServiceLevel(1, 1, 1));
        final Account b = new Account("name1");
        final Account c = new Account(10L, "name2", new ServiceLevel());

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
        final Account a = new Account("name1", new ServiceLevel(1, 1, 1));
        final Account b = new Account("name1");
        final Account c = new Account(10L, "name2", new ServiceLevel());

        assertEquals(1506671220, a.hashCode());
        assertEquals(1513053572, b.hashCode());
        assertEquals(1513561471, c.hashCode());
    }

    @Test
    public void testToString() {
        final Account a = new Account("name", new ServiceLevel(1, 2, 3));
        final Account b = new Account(10L, "name", new ServiceLevel());
        assertEquals(
                "Account[id=Optional.empty,name=name,serviceLevel=ServiceLevel[maxGroups=1,maxTags=2,maxDepth=3],"
                        + "apiLogins=[]]",
                a.toString());
        assertEquals(
                "Account[id=Optional[10],name=name,serviceLevel=ServiceLevel[maxGroups=100,maxTags=1000,maxDepth=3],"
                        + "apiLogins=[]]",
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
    public void testWithNullId() {
        new Account(null, new ServiceLevel());
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testWithNullServiceLevel() {
        new Account("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTooLongName() {
        new Account(StringUtils.leftPad("", Account.Validator.MAX_NAME_LENGTH + 1, "N"), new ServiceLevel());
    }

    @Test
    public void testWithLongName() {
        final String name = StringUtils.leftPad("", Account.Validator.MAX_NAME_LENGTH, "N");
        final Account account = new Account(name, new ServiceLevel());

        assertEquals(name, account.getName());
    }

    @Test
    public void testCopy() {
        final Account original = new Account("name", new ServiceLevel());
        final Account copy = new Account(original);

        assertEquals(original, copy);
    }

    @Test
    public void testDefaultConstructor() {
        final Account account = new Account();

        assertFalse(account.getId().isPresent());
        assertEquals("", account.getName());
        assertNotNull(account.getServiceLevel());
    }
}
