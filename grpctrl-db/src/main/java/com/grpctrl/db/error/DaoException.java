package com.grpctrl.db.error;

import javax.annotation.Nonnull;

/**
 * A generic exception thrown by the data access layer when there are problems managing group data in the database.
 */
public class DaoException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public DaoException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public DaoException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public DaoException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
