package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class AccountRemove extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountRemove(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
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
    public RemoveResponse remove(@Nonnull @PathParam("accountId") final Long accountId) {
        // TODO: Only admins should be able to retrieve accounts.

        return new RemoveResponse(getAccountDao().remove(accountId));
    }

    @JsonPropertyOrder({"success", "removed"})
    private static class RemoveResponse {
        private final boolean success;
        private final boolean removed;

        public RemoveResponse(final int count) {
            this.success = true;
            this.removed = count > 0;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public boolean isRemoved() {
            return this.removed;
        }
    }
}
