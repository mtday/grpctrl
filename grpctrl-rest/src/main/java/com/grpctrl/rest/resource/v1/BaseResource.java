package com.grpctrl.rest.resource.v1;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.rest.providers.AccountLookupFilter;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

/**
 * The base class for resources.
 */
public class BaseResource {
    public Optional<Account> getAccount(@Nonnull final ContainerRequestContext requestContext) {
        return Optional.ofNullable((Account) requestContext.getProperty(AccountLookupFilter.ACCOUNT_PROPERTY));
    }

    public Account requireAccount(@Nonnull final ContainerRequestContext requestContext) {
        final Optional<Account> account = getAccount(requestContext);
        if (!account.isPresent()) {
            throw new BadRequestException("API key and secret must be provided for this request");
        }
        return account.get();
    }

    public Optional<User> getUser(@Nonnull final SecurityContext securityContext) {
        return Optional.ofNullable((User) Objects.requireNonNull(securityContext).getUserPrincipal());
    }

    public boolean hasRole(@Nonnull final SecurityContext securityContext, @Nonnull final UserRole userRole) {
        final Optional<User> user = getUser(Objects.requireNonNull(securityContext));
        return user.isPresent() && user.get().getRoles().contains(Objects.requireNonNull(userRole));
    }

    public void requireRole(@Nonnull final SecurityContext securityContext, @Nonnull final UserRole userRole) {
        if (!hasRole(securityContext, userRole)) {
            throw new ForbiddenException("Access to resource requires role: " + userRole.name());
        }
    }
}
