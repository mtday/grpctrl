package com.grpctrl.rest.resource.v1.group;

import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.GroupDaoSupplier;
import com.grpctrl.rest.resource.v1.account.MultipleAccountStreamer;

import java.io.InputStream;

import javax.annotation.Nonnull;
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
public class GroupAdd extends BaseGroupResource {
    @Inject
    public GroupAdd(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final GroupDaoSupplier groupDaoSupplier) {
        super(objectMapperSupplier, groupDaoSupplier);
    }

    @POST
    public Response add(
            @Context @Nonnull final SecurityContext securityContext, @Nonnull final InputStream inputStream) {
        // TODO: Determine the account based on the user/apikey security
        //final Account account = new Account().setId(10001L).setName("parent");

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapperSupplier(), consumer -> {
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}
