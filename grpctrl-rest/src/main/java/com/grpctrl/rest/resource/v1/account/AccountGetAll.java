package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.common.model.Account;
import com.grpctrl.db.dao.AccountDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
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
@RolesAllowed("ADMIN")
public class AccountGetAll extends BaseAccountResource {
    private static final Logger LOG = LoggerFactory.getLogger(AccountGetAll.class);

    private final Consumer<Consumer<Account>> consumer = consumer -> getAccountDao().getAll(consumer);

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
    public Response getAll(@Nonnull @Context SecurityContext securityContext) {
        final Principal userPrincipal = securityContext.getUserPrincipal();
        LOG.info("User Principal Class: {}", userPrincipal.getClass().getName());
        LOG.info("User Principal: {}", userPrincipal);
        LOG.info("Authentication Scheme: {}", securityContext.getAuthenticationScheme());
        LOG.info("Is Secure? {}", securityContext.isSecure());

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapper(), this.consumer);

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
