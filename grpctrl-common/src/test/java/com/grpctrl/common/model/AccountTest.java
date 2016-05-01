package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Perform testing on the {@link Account} class.
 */
public class AccountTest {
    @Test
    public void testCompareTo() {
        final Account a = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();
        final Account b = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxChildren(3).setMaxDepth(4).build())
                .build();
        final Account c = new Account.Builder("id2",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();

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
        final Account a = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();
        final Account b = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxChildren(3).setMaxDepth(4).build())
                .build();
        final Account c = new Account.Builder("id2",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();

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
        final Account a = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();
        final Account b = new Account.Builder("id1",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxChildren(3).setMaxDepth(4).build())
                .build();
        final Account c = new Account.Builder("id2",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(1).setMaxChildren(1).setMaxDepth(1).build())
                .build();

        assertEquals(35786068, a.hashCode());
        assertEquals(35787514, b.hashCode());
        assertEquals(35786105, c.hashCode());
    }

    @Test
    public void testToString() {
        final Account account = new Account.Builder("id",
                new ServiceLevel.Builder().setMaxGroups(1).setMaxTags(2).setMaxChildren(3).setMaxDepth(4).build())
                .build();
        assertEquals(
                "Account[id=id,serviceLevel=ServiceLevel[maxGroups=1,maxTags=2,maxChildren=3,maxDepth=4]]",
                account.toString());
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

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullId() {
        new Account.Builder(null, Mockito.mock(ServiceLevel.class));
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullServiceLevel() {
        new Account("id", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongId() {
        new Account(
                StringUtils.leftPad("", Account.Validator.MAX_ID_LENGTH + 1, "I"), Mockito.mock(ServiceLevel.class));
    }

    @Test
    public void testBuilderWithLongId() {
        final String id = StringUtils.leftPad("", Account.Validator.MAX_ID_LENGTH, "I");
        final Account account = new Account(id, Mockito.mock(ServiceLevel.class));

        assertEquals(id, account.getId());
    }
}
