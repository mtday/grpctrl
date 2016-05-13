package com.grpctrl.rest.providers;

import com.google.common.base.Charsets;
import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserAuth;
import com.grpctrl.common.model.UserSource;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 * Injects user information into the request based on input headers.
 */
@Provider
public class UserLookupFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(UserLookupFilter.class);

    @Nonnull
    private final UserDaoSupplier userDaoSupplier;

    @Inject
    public UserLookupFilter(@Nonnull final UserDaoSupplier userDaoSupplier) {
        this.userDaoSupplier = Objects.requireNonNull(userDaoSupplier);
    }

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext) throws IOException {
        final SecurityContext securityContext = requestContext.getSecurityContext();
        final Principal principal = securityContext.getUserPrincipal();
        if (principal != null) {
            LOG.warn("Security context already has a principal: {}", principal);
            return;
        }

        final Optional<Pair<String, String>> login = getAuthorization(requestContext);

        if (login.isPresent()) {
            // Fetch the corresponding user.
            final Optional<User> user = this.userDaoSupplier.get().get(UserSource.LOCAL, login.get().getKey());
            if (!user.isPresent()) {
                throw new ForbiddenException("User login or password invalid (user not found)");
            }

            // Validate the user password.
            final Optional<UserAuth> userAuth = user.get().getUserAuth();
            if (!userAuth.isPresent()) {
                throw new ForbiddenException("User login or password invalid (user auth not present)");
            }
            if (!userAuth.get().validate(login.get().getValue())) {
                throw new ForbiddenException("User login or password invalid (password validation failed)");
            }

            // Update the security context for this user.
            requestContext.setSecurityContext(user.get());
        }
    }

    private Optional<Pair<String, String>> getAuthorization(@Nonnull final ContainerRequestContext requestContext) {
        final String header = requestContext.getHeaderString("Authorization");
        if (header == null) {
            // No authorization header provided, it may not be needed so we don't thrown an exception here.
            return Optional.empty();
        }

        if (!StringUtils.startsWithIgnoreCase(header, "basic ")) {
            throw new BadRequestException("The authorization header is not digest encoded: " + header);
        }

        final List<String> parts = Arrays.asList(header.split(" "));
        if (parts.size() != 2) {
            throw new BadRequestException("The authorization header was malformed, need two parts: " + header);
        }

        final String encoded = StringUtils.trimToEmpty(parts.get(1));
        if (StringUtils.isEmpty(encoded)) {
            throw new BadRequestException(
                    "The authorization header was malformed, encoded authorization was empty: " + header);
        }

        final String decoded = Base64.decodeAsString(encoded.getBytes(Charsets.UTF_8));
        final List<String> values = Arrays.asList(decoded.split(":"));
        if (values.size() != 2) {
            throw new BadRequestException("The authorization header was malformed, need two decoded values: " + header);
        }

        final String username = values.get(0);
        final String password = values.get(1);
        if (StringUtils.isEmpty(username)) {
            throw new BadRequestException("The authorization header was malformed, user name was empty: " + header);
        }
        if (StringUtils.isEmpty(password)) {
            throw new BadRequestException("The authorization header was malformed, password was empty: " + header);
        }

        return Optional.of(ImmutablePair.of(username, password));
    }
}
