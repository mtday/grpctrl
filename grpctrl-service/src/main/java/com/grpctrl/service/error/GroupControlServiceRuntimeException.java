package com.grpctrl.service.error;

import javax.annotation.Nonnull;

/**
 * A generic exception thrown by the {@link com.grpctrl.service.GroupControlService} when there are irrecoverable
 * problems managing group data. This is a runtime exception because these are not intended to be recoverable.
 */
public class GroupControlServiceRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public GroupControlServiceRuntimeException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public GroupControlServiceRuntimeException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public GroupControlServiceRuntimeException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
