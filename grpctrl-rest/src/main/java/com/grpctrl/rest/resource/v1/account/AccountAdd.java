package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.model.Account;
import com.grpctrl.db.GroupControlDao;
import com.grpctrl.db.error.DaoException;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Add an account to the backing data store.
 */
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountAdd {
    @Nonnull
    private final GroupControlDao groupControlDao;

    /**
     * @param groupControlDao the {@link GroupControlDao} used to perform the account operation
     */
    @Inject
    public AccountAdd(@Nonnull final GroupControlDao groupControlDao) {
        this.groupControlDao = Objects.requireNonNull(groupControlDao);
    }

    @Nonnull
    private GroupControlDao getGroupControlDao() {
        return this.groupControlDao;
    }

    /**
     * Save the provided account into the backing data store.
     *
     * @param account the new account object to be added to the backing data store
     *
     * @return the updated account, including new unique identifiers
     *
     * @throws DaoException if there is a problem communicating with the data store.
     */
    @POST
    public Account add(@Nonnull final Account account) throws DaoException {
        // TODO: Only admins should be able to add accounts.

        return getGroupControlDao().addAccount(account);
    }
}
