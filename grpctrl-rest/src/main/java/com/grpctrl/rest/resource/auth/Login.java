package com.grpctrl.rest.resource.auth;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/auth/login")
public class Login {
    private static final Logger LOG = LoggerFactory.getLogger(Login.class);

    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;

    @Inject
    public Login(@Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier) {
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);
    }

    public OAuth20ServiceSupplier getOAuth20ServiceSupplier() {
        return this.oAuth20ServiceSupplier;
    }

    @GET
    public Response login() {
        LOG.info("Fetching OAuth2 Token");
        final OAuth20Service oAuthService = getOAuth20ServiceSupplier().get();
        return Response.seeOther(URI.create(oAuthService.getAuthorizationUrl())).build();
    }
}
