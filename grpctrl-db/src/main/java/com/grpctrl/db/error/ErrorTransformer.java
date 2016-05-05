package com.grpctrl.db.error;

import org.apache.commons.lang3.StringUtils;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

/**
 * Transforms database errors into web errors.
 */
public class ErrorTransformer {
    /**
     * Transforms database exceptions into an exception that can be sent back to the web request client.
     *
     * @param message the message to include in the exception back to the client
     * @param sqlException the database exception to transform
     *
     * @return an appropriate client exception based on the provided input exception
     */
    public static WebApplicationException get(@Nonnull final String message, @Nonnull final SQLException sqlException) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(sqlException);

        if (sqlException instanceof BatchUpdateException) {
            return get(message, sqlException.getNextException());
        } else if (StringUtils.containsIgnoreCase(sqlException.getMessage(), "unique")) {
            if (StringUtils.containsIgnoreCase(sqlException.getMessage(), "groups_uniq_name")) {
                return new BadRequestException(
                        message + " - the group name must be unique within the same parent", sqlException);
            }
            return new BadRequestException(message + " - unique constraint violation", sqlException);
        } else if (StringUtils.containsIgnoreCase(sqlException.getMessage(), "foreign")) {
            if (StringUtils.containsIgnoreCase(sqlException.getMessage(), "groups_fk_parent")) {
                return new BadRequestException(
                        message + " - the parent id does not exist in your account", sqlException);
            }
            return new BadRequestException(message + " - foreign key violation", sqlException);
        } else {
            return new InternalServerErrorException(message, sqlException);
        }
    }
}
