package com.grpctrl.db.error;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

/**
 * An exception thrown by the data access layer when an insert operation would go beyond the service level quota for
 * an account.
 */
public class QuotaExceededException extends ClientErrorException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public QuotaExceededException(@Nonnull final String message) {
        super(message, Response.Status.PAYMENT_REQUIRED);
    }

    /**
     * @param cause the cause of the exception
     */
    public QuotaExceededException(@Nonnull final Throwable cause) {
        super(Response.Status.PAYMENT_REQUIRED, cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public QuotaExceededException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, Response.Status.PAYMENT_REQUIRED, cause);
    }
}
