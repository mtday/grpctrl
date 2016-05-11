package com.grpctrl.run;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class AuthenticatorImpl implements Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticatorImpl.class);

    @Nullable
    private AuthConfiguration authConfiguration;

    @Override
    public void setConfiguration(@Nonnull final AuthConfiguration authConfiguration) {
        LOG.info("setConfiguration");
        LOG.info("  authConfiguration: {}", authConfiguration);
        this.authConfiguration = Objects.requireNonNull(authConfiguration);
    }

    @Override
    public String getAuthMethod() {
        LOG.info("getAuthMethod");
        return "OAUTH";
    }

    @Override
    public void prepareRequest(@Nonnull final ServletRequest request) {
        LOG.info("prepareRequest");
        LOG.info("  request: {}", request);
    }

    @Override
    public Authentication validateRequest(
            @Nonnull final ServletRequest request, @Nonnull final ServletResponse response, final boolean mandatory)
            throws ServerAuthException {
        LOG.info("validateRequest");
        LOG.info("  request: {}", request);
        LOG.info("  response: {}", response);
        LOG.info("  mandatory: {}", mandatory);
        return Authentication.SEND_SUCCESS;
    }

    @Override
    public boolean secureResponse(
            @Nonnull final ServletRequest request, @Nonnull final ServletResponse response, final boolean mandatory,
            @Nonnull final Authentication.User validatedUser) throws ServerAuthException {
        LOG.info("validateRequest");
        LOG.info("  request: {}", request);
        LOG.info("  response: {}", response);
        LOG.info("  mandatory: {}", mandatory);
        LOG.info("  validatedUser: {}", validatedUser);
        return true;
    }
}
