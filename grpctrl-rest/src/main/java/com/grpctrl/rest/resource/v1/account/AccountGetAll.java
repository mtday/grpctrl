package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

/**
 * Retrieve all of the accounts in the system.
 */
@Singleton
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
public class AccountGetAll extends BaseAccountResource {
    private final Consumer<Consumer<Account>> consumer = consumer -> getAccountDaoSupplier().get().getAll(consumer);

    @Inject
    public AccountGetAll(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        super(objectMapperSupplier, accountDaoSupplier);
    }

    @GET
    @Nullable
    public Response getAll(@Nonnull @Context SecurityContext securityContext) {
        requireRole(securityContext, UserRole.ADMIN);

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapperSupplier(), this.consumer);

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
