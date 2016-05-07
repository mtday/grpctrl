package com.grpctrl.common.model.rest;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents an error message to be sent back to the client.
 */
public class ErrorResponse {
    private final boolean success;
    private final int code;
    private final String message;

    public ErrorResponse(final int code, @Nonnull final String message) {
        this.success = false;
        this.code = code;
        this.message = Objects.requireNonNull(message);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
