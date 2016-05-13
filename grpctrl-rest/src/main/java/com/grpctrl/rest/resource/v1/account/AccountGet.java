package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Retrieve the account information for the provided unique account identifier.
 */
@Singleton
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AccountGet extends BaseAccountResource {
    /**
     * @param objectMapperSupplier the {@link ObjectMapperSupplier} responsible for generating JSON data
     * @param accountDaoSupplier the {@link AccountDaoSupplier} used to perform the account operation
     */
    @Inject
    public AccountGet(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        super(objectMapperSupplier, accountDaoSupplier);
    }

    /**
     * Retrieve an account from the backing data store.
     *
     * @param accountId the unique identifier of the account to retrieve
     *
     * @return the response containing the requested account, if available
     */
    @GET
    @Nullable
    public Response get(@Nonnull @PathParam("accountId") final Long accountId) {
        final StreamingOutput streamingOutput = new SingleAccountStreamer(getObjectMapperSupplier(),
                consumer -> getAccountDaoSupplier().get().get(accountId, consumer));

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
