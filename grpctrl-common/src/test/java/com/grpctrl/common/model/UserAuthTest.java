package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link UserAuth} class.
 */
public class UserAuthTest {
    @Test
    public void testCompareTo() {
        final UserAuth a = new UserAuth("MD5", "abcd", "hashed_password");
        final UserAuth b = new UserAuth("MD5", "abcd", "hashed_password1");
        final UserAuth c = new UserAuth("MD5", "abce", "hashed_password");
        final UserAuth d = new UserAuth("SHA-256", "abcd", "hashed_password");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(1, a.compareTo(c));
        assertEquals(-6, a.compareTo(d));
        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(-6, b.compareTo(d));
        assertEquals(-1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(6, d.compareTo(a));
        assertEquals(6, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final UserAuth a = new UserAuth("MD5", "abcd", "hashed_password");
        final UserAuth b = new UserAuth("MD5", "abcd", "hashed_password1");
        final UserAuth c = new UserAuth("MD5", "abce", "hashed_password");
        final UserAuth d = new UserAuth("SHA-256", "abcd", "hashed_password");

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
        final UserAuth a = new UserAuth("MD5", "abcd", "hashed_password");
        final UserAuth b = new UserAuth("MD5", "abcd", "hashed_password1");
        final UserAuth c = new UserAuth("MD5", "abce", "hashed_password");
        final UserAuth d = new UserAuth("SHA-256", "abcd", "hashed_password");

        assertEquals(-521682830, a.hashCode());
        assertEquals(-1166625431, b.hashCode());
        assertEquals(-521682793, c.hashCode());
        assertEquals(525865830, d.hashCode());
    }

    @Test
    public void testToString() {
        final UserAuth auth = new UserAuth("MD5", "abcd", "hashed_password");
        assertEquals("UserAuth[hashAlgorithm=MD5,salt=abcd,hashedPass=hashed_password]", auth.toString());
    }

    @Test
    public void testCopy() {
        final UserAuth original = new UserAuth("MD5", "abcd", "hashed_password");
        final UserAuth copy = new UserAuth(original);

        assertEquals(original, copy);
    }

    @Test
    public void testDefaultConstructor() {
        final UserAuth auth = new UserAuth();

        assertEquals("SHA-512", auth.getHashAlgorithm());
        assertEquals("", auth.getSalt());
        assertEquals("", auth.getHashedPass());
    }

    @Test
    public void testFromPassword() {
        final UserAuth auth = UserAuth.fromPassword("password");

        assertEquals("SHA-512", auth.getHashAlgorithm());
        assertEquals(20, auth.getSalt().length());
        assertEquals(128, auth.getHashedPass().length());
    }

    @Test
    public void testFromPasswordSHA256() {
        final UserAuth auth = UserAuth.fromPassword("SHA-256", "password");
        System.out.println(auth);

        assertEquals("SHA-256", auth.getHashAlgorithm());
        assertEquals(20, auth.getSalt().length());
        assertEquals(64, auth.getHashedPass().length());
    }

    @Test
    public void testValidate() {
        final UserAuth auth = UserAuth.fromPassword("password");
        assertTrue(auth.validate("password"));
    }
}
