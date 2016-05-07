package com.grpctrl.rest.providers;

import com.grpctrl.common.model.rest.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Process exceptions into JSON to send back to the caller.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(@Nonnull final Throwable throwable) {
        LOG.error("Exception caught", throwable);
        if (throwable instanceof WebApplicationException) {
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
