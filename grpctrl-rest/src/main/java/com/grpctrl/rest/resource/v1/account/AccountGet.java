package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.error.DaoException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Retrieve the account information for the provided unique account identifier.
 */
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountGet extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountGet(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        super(objectMapper, accountDao);
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
        // TODO: Only admins should be able to retrieve accounts.

        final StreamingOutput streamingOutput = new SingleAccountStreamer(getObjectMapper(), consumer -> {
            try {
                getAccountDao().get(consumer, accountId);
            } catch (final DaoException daoException) {
                throw new InternalServerErrorException("Failed to add account to backing store", daoException);
            }
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
