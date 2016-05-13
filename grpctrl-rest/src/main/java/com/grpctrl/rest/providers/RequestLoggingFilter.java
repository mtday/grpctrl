package com.grpctrl.rest.providers;

import com.grpctrl.common.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

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
        LOG.info(String.format("%-20s  %-6s %s", getLogin(requestContext), requestContext.getMethod(),
                requestContext.getUriInfo().getAbsolutePath().getPath()));
    }

    @Nonnull
    private String getLogin(@Nonnull final ContainerRequestContext requestContext) {
        final Optional<User> user = Optional.ofNullable((User) requestContext.getSecurityContext().getUserPrincipal());
        if (user.isPresent()) {
            return user.get().getUserSource().name().toLowerCase() + ":" + user.get().getLogin();
        }
        return "-"; // User not logged in.
    }
}
