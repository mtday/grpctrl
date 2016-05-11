package com.grpctrl.rest.resource.v1.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.rest.resource.v1.account.MultipleAccountStreamer;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

/**
 * Add groups to the backing data store.
 */
@Singleton
@Path("/v1/group/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class GroupAdd extends BaseGroupResource {
    /**
     * @param objectMapper the {@link ObjectMapper} used to generate JSON data
     * @param groupDao the {@link GroupDao} used to perform the group operation
     */
    @Inject
    public GroupAdd(@Nonnull final ObjectMapper objectMapper, @Nonnull final GroupDao groupDao) {
        super(objectMapper, groupDao);
    }

    /**
     * Save the provided groups into the backing data store.
     *
     * @param securityContext the {@link SecurityContext} used to retrieve account information about the caller
     * @param inputStream the {@link InputStream} from the client containing the list of accounts to add
     *
     * @return the response containing the updated account, including new unique identifiers
     */
    @POST
    public Response add(
            @Context @Nonnull final SecurityContext securityContext, @Nonnull final InputStream inputStream) {
        // TODO: Determine the account based on the user/apikey security
        //final Account account = new Account().setId(10001L).setName("parent");

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapper(), consumer -> {
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
