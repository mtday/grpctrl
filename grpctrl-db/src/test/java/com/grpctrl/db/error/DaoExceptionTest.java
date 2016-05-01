package com.grpctrl.db.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing on the {@link DaoException} class.
 */
public class DaoExceptionTest {
    @Test
    public void testStringConstructor() {
        final DaoException exception = new DaoException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final DaoException exception = new DaoException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final DaoException exception = new DaoException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
