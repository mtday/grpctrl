package com.grpctrl.client.error;

import javax.annotation.Nonnull;

/**
 * A generic exception thrown by the REST clients when there are problems communicating, sending, or parsing data.
 */
public class ClientException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public ClientException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public ClientException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public ClientException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
