package com.grpctrl.service.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing on the {@link GroupControlServiceRuntimeException} class.
 */
public class GroupControlServiceRuntimeExceptionTest {
    @Test
    public void testStringConstructor() {
        final GroupControlServiceRuntimeException exception = new GroupControlServiceRuntimeException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final GroupControlServiceRuntimeException exception = new GroupControlServiceRuntimeException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final GroupControlServiceRuntimeException exception = new GroupControlServiceRuntimeException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
