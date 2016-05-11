package com.grpctrl.rest.resource.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/auth/logout")
public class Logout {
    private static final Logger LOG = LoggerFactory.getLogger(Logout.class);

    @GET
    public Response logout(@Context @Nonnull final UriInfo uriInfo, @Context @Nonnull HttpServletRequest request) throws ServletException {
        LOG.info("Logging out");
        request.logout();
        return Response.seeOther(UriBuilder.fromUri(uriInfo.getBaseUri()).build()).build();
    }
}
