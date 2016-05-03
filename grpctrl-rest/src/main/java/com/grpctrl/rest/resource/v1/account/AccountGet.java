package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.model.Account;
import com.grpctrl.db.GroupControlDao;
import com.grpctrl.db.error.DaoException;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Retrieve the account information for the provided unique account identifier.
 */
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountGet {
    @Nonnull
    private final GroupControlDao groupControlDao;

    /**
     * @param groupControlDao the {@link GroupControlDao} used to perform the account operation
     */
    @Inject
    public AccountGet(@Nonnull final GroupControlDao groupControlDao) {
        this.groupControlDao = Objects.requireNonNull(groupControlDao);
    }

    @Nonnull
    private GroupControlDao getGroupControlDao() {
        return this.groupControlDao;
    }

    /**
     * Retrieve an account from the backing data store.
     *
     * @param accountId the unique identifier of the account to retrieve
     *
     * @return the requested account, if available
     *
     * @throws DaoException if there is a problem communicating with the data store.
     */
    @GET
    @Nullable
    public Account get(@Nonnull @PathParam("accountId") final Long accountId) throws DaoException {
        return getGroupControlDao().getAccount(accountId).orElse(null);
    }
}
