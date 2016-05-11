package com.grpctrl.rest.providers;

import com.grpctrl.common.model.rest.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Process exceptions into JSON to send back to the caller.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Inject
    @Nullable
    private UriInfo uriInfo;

    @Nonnull
    protected Optional<UriInfo> getUriInfo() {
        return Optional.ofNullable(this.uriInfo);
    }

    @Nonnull
    protected String getAbsolutePath() {
        final Optional<UriInfo> uri = getUriInfo();
        if (uri.isPresent()) {
            return uri.get().getAbsolutePath().getPath();
        }
        return ""; // No UriInfo available?
    }

    @Override
    public Response toResponse(@Nonnull final Throwable throwable) {
        if (throwable instanceof NotFoundException) {
            LOG.error("404 Not Found: {}", getAbsolutePath());
        } else if (throwable instanceof ForbiddenException) {
            LOG.error("User forbidden access to: {}", getAbsolutePath());
        } else if (throwable instanceof WebApplicationException) {
            LOG.error("Exception caught", throwable);
            final WebApplicationException wae = (WebApplicationException) throwable;
            return Response.status(wae.getResponse().getStatus())
                    .entity(new ErrorResponse(wae.getResponse().getStatus(), throwable.getMessage()))
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        throwable.getMessage())).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
