package com.grpctrl.rest.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * Performs logging of web requests.
 */
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext) throws IOException {
        LOG.info(String.format("%-6s  %s", requestContext.getMethod(),
                requestContext.getUriInfo().getAbsolutePath().getPath()));
    }
}
