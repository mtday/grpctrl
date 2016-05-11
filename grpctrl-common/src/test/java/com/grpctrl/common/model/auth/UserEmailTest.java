package com.grpctrl.common.model.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.grpctrl.common.model.UserEmail;

import org.junit.Test;

/**
 * Perform testing on the {@link UserEmail} class.
 */
public class UserEmailTest {
    @Test
    public void testCompareTo() {
        final UserEmail a = new UserEmail("email1@email.com");
        final UserEmail b = new UserEmail("email1@email.com", true, false);
        final UserEmail c = new UserEmail("email2@email.com", false, false);

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
        final UserEmail a = new UserEmail("email1@email.com");
        final UserEmail b = new UserEmail("email1@email.com", true, false);
        final UserEmail c = new UserEmail("email2@email.com", false, false);

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
        final UserEmail a = new UserEmail("email1@email.com");
        final UserEmail b = new UserEmail("email1@email.com", true, false);
        final UserEmail c = new UserEmail("email2@email.com", false, false);

        assertEquals(1514878161, a.hashCode());
        assertEquals(1514878162, b.hashCode());
        assertEquals(-1790238960, c.hashCode());
    }

    @Test
    public void testToString() {
        final UserEmail email = new UserEmail("email@email.com", true, false);
        assertEquals("UserEmail[email=email@email.com,primary=true,verified=false]", email.toString());
    }

    @Test
    public void testCopy() {
        final UserEmail original = new UserEmail("email@email", false, true);
        final UserEmail copy = new UserEmail(original);

        assertEquals(original, copy);
    }

    @Test
    public void testDefaultConstructor() {
        final UserEmail email = new UserEmail();

        assertEquals("", email.getEmail());
        assertTrue(email.isPrimary());
        assertTrue(email.isVerified());
    }
}
