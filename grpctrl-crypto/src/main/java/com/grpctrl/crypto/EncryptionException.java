package com.grpctrl.crypto;

import javax.annotation.Nonnull;

/**
 * An exception thrown by the encryption classes.
 */
public class EncryptionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the error message associated with the exception
     */
    public EncryptionException(@Nonnull final String message) {
        super(message);
    }

    /**
     * @param cause the cause of the exception
     */
    public EncryptionException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the error message associated with the exception
     * @param cause the cause of the exception
     */
    public EncryptionException(@Nonnull final String message, @Nonnull final Throwable cause) {
        super(message, cause);
    }
}
