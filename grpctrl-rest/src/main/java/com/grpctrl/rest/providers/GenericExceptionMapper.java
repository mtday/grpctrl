package com.grpctrl.rest.providers;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.rest.ErrorResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Process exceptions into JSON to send back to the caller.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Nonnull
    private final UriInfo uriInfo;

    @Nonnull
    private final SecurityContext securityContext;

    @Inject
    public GenericExceptionMapper(@Nonnull final UriInfo uriInfo, @Nonnull final SecurityContext securityContext) {
        this.uriInfo = Objects.requireNonNull(uriInfo);
        this.securityContext = Objects.requireNonNull(securityContext);
    }

    @Nonnull
    private String getUserLogin() {
        final Optional<User> user = Optional.ofNullable((User) this.securityContext.getUserPrincipal());
        if (user.isPresent()) {
            return user.get().getUserSource().name().toLowerCase() + ":" + user.get().getLogin();
        }
        return "-";
    }

    @Override
    public Response toResponse(@Nonnull final Throwable throwable) {
        final String absolutePath = this.uriInfo.getAbsolutePath().getPath();

        if (throwable instanceof NotFoundException) {
            LOG.error("404 Not Found: {}", absolutePath);
        } else if (throwable instanceof ForbiddenException) {
            LOG.error("User {} forbidden access to: {}", getUserLogin(), absolutePath);
        } else if (throwable instanceof WebApplicationException) {
            LOG.error("Exception caught", throwable);
            final WebApplicationException wae = (WebApplicationException) throwable;
            return Response.status(wae.getResponse().getStatus())
                    .entity(new ErrorResponse(wae.getResponse().getStatus(), throwable.getMessage()))
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            LOG.error("Exception caught", throwable);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                        ExceptionUtils.getMessage(throwable))).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
