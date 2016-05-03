package com.grpctrl.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.grpctrl.crypto.EncryptionException;

import org.junit.Test;

/**
 * Perform testing of the {@link EncryptionException} class.
 */
public class EncryptionExceptionTest {
    @Test
    public void testStringConstructor() {
        final EncryptionException exception = new EncryptionException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final EncryptionException exception = new EncryptionException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final EncryptionException exception = new EncryptionException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
