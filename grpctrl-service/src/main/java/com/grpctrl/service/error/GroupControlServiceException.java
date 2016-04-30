package com.grpctrl.service.error;

import javax.annotation.Nonnull;

/**
 * A generic exception thrown by the {@link com.grpctrl.service.GroupControlService} when there are problems managing
 * group data.
 */
public class GroupControlServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public GroupControlServiceException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public GroupControlServiceException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public GroupControlServiceException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
