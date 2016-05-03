package com.grpctrl.db.error;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the data access layer when an insert operation would go beyond the service level quota for
 * an account.
 */
public class QuotaExceededException extends DaoException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public QuotaExceededException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public QuotaExceededException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public QuotaExceededException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
