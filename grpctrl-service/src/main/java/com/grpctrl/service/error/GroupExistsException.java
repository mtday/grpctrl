package com.grpctrl.service.error;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the {@link com.grpctrl.service.GroupControlService} in situations where a group is expected to
 * not exist, but does.
 */
public class GroupExistsException extends GroupControlServiceException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public GroupExistsException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public GroupExistsException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public GroupExistsException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
