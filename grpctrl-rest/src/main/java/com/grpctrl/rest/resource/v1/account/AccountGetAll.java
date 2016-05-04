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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Retrieve all of the accounts in the system.
 */
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
public class AccountGetAll extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountGetAll(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        super(objectMapper, accountDao);
    }

    /**
     * Retrieve all accounts from the backing data store.
     *
     * @return the response containing all the accounts
     */
    @GET
    @Nullable
    public Response getAll() {
        // TODO: Only admins should be able to retrieve accounts.

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapper(), consumer -> {
            try {
                getAccountDao().getAll(consumer);
            } catch (final DaoException daoException) {
                throw new InternalServerErrorException("Failed to add account to backing store", daoException);
            }
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
