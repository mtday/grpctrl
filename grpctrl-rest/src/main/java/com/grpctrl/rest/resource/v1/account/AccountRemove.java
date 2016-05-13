package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Delete the account information for the provided unique account identifier.
 */
@Singleton
@Path("/v1/account/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountRemove extends BaseAccountResource {
    @Inject
    public AccountRemove(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        super(objectMapperSupplier, accountDaoSupplier);
    }

    @DELETE
    @Nullable
    public RemoveResponse remove(
            @Nonnull @Context final SecurityContext securityContext,
            @Nonnull @PathParam("accountId") final Long accountId) {
        requireRole(securityContext, UserRole.ADMIN);

        return new RemoveResponse(getAccountDaoSupplier().get().remove(accountId));
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
