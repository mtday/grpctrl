package com.grpctrl.rest.resource.v1.status;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.User;
import com.grpctrl.rest.resource.v1.BaseResource;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Retrieve account information for the current user and account.
 */
@Singleton
@Path("/v1/status")
@Produces(MediaType.APPLICATION_JSON)
public class AccountStatus extends BaseResource {
    @GET
    @Nullable
    public Response get(
            @Nonnull @Context final SecurityContext securityContext,
            @Nonnull @Context final ContainerRequestContext requestContext) {
        final Optional<Account> account = getAccount(requestContext);
        final Optional<User> user = getUser(securityContext);

        final AccountStatusResponse response = new AccountStatusResponse(account.orElse(null), user.orElse(null));

        return Response.ok().entity(response).type(MediaType.APPLICATION_JSON).build();
    }
}
