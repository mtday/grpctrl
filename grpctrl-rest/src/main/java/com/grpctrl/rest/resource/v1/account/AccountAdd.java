package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.common.model.Account;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.error.DaoException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Add an account to the backing data store.
 */
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountAdd extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} used to generate JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountAdd(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        super(objectMapper, accountDao);
    }

    /**
     * Save the provided account into the backing data store.
     *
     * @param account the new account object to be added to the backing data store
     *
     * @return the response containing the updated account, including new unique identifiers
     */
    @POST
    public Response add(@Nonnull final Account account) {
        // TODO: Only admins should be able to add accounts.

        final StreamingOutput streamingOutput = new AccountStreamer(getObjectMapper(), consumer -> {
            try {
                getAccountDao().add(consumer, account);
            } catch (final DaoException daoException) {
                throw new InternalServerErrorException("Failed to add account to backing store", daoException);
            }
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
