package com.grpctrl.service.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Perform testing on the {@link GroupControlServiceException} class.
 */
public class GroupControlServiceExceptionTest {
    @Test
    public void testStringConstructor() {
        final GroupControlServiceException exception = new GroupControlServiceException("error");
        assertEquals("error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testThrowableConstructor() {
        final Exception cause = new Exception();
        final GroupControlServiceException exception = new GroupControlServiceException(cause);
        assertEquals("java.lang.Exception", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testStringThrowableConstructor() {
        final Exception cause = new Exception();
        final GroupControlServiceException exception = new GroupControlServiceException("error", cause);
        assertEquals("error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
