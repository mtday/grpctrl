package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.db.dao.AccountDao;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Delete the account information for the provided unique account identifier.
 */
@Singleton
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountDelete extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountDelete(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        super(objectMapper, accountDao);
    }

    /**
     * Remove an account from the backing data store.
     *
     * @param accountId the unique identifier of the account to remove
     *
     * @return the response indicating whether the account was removed successfully
     */
    @DELETE
    @Nullable
    public DeleteResponse remove(@Nonnull @PathParam("accountId") final Long accountId) {
        // TODO: Only admins should be able to retrieve accounts.

        return new DeleteResponse(getAccountDao().remove(accountId));
    }

    private static class DeleteResponse {
        private boolean removed;

        public DeleteResponse(final int count) {
            this.removed = count > 0;
        }

        public boolean isRemoved() {
            return this.removed;
        }

        public void setRemoved(final boolean removed) {
            this.removed = removed;
        }
    }
}
