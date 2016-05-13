package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

/**
 * Retrieve the account information for the provided unique account identifier.
 */
@Singleton
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountGet extends BaseAccountResource {
    @Inject
    public AccountGet(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        super(objectMapperSupplier, accountDaoSupplier);
    }

    @GET
    @Nullable
    public Response get(
            @Nonnull @Context final SecurityContext securityContext,
            @Nonnull @PathParam("accountId") final Long accountId) {
        requireRole(securityContext, UserRole.ADMIN);

        final StreamingOutput streamingOutput = new SingleAccountStreamer(getObjectMapperSupplier(),
                consumer -> getAccountDaoSupplier().get().get(accountId, consumer));

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
