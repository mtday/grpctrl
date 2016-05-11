package com.grpctrl.security;

import org.eclipse.jetty.jaas.spi.AbstractLoginModule;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

public class CustomLoginModule extends AbstractLoginModule {
    private static final Logger LOG = LoggerFactory.getLogger(CustomLoginModule.class);

    public boolean login() throws LoginException {
        try {
            if (isIgnored()) {
                return false;
            }

            final Callback[] callbacks = configureCallbacks();
            getCallbackHandler().handle(callbacks);

            final UserInfo userInfo = getUserInfo(callbacks);

            if (userInfo == null) {
                setAuthenticated(false);
                throw new FailedLoginException();
            }

            setCurrentUser(new JAASUserInfo(userInfo));
            setAuthenticated(true);

            if (isAuthenticated()) {
                getCurrentUser().fetchRoles();
                return true;
            }
            throw new FailedLoginException();
        } catch (final Exception exception) {
            LOG.error("Failed to perform login", exception);
            throw new LoginException("Failed to perform login");
        }
    }

    @Override
    public UserInfo getUserInfo(@Nonnull final String username) throws Exception {
        return null; // Not used.
    }

    public UserInfo getUserInfo(@Nonnull final Callback[] callbacks) throws Exception {
        for (final Callback callback : callbacks) {
            if (callback instanceof CustomCallbackHandler.CustomCallback) {
                return ((CustomCallbackHandler.CustomCallback) callback).getUserInfo();
            }
        }
        return null;
    }

    @Override
    public Callback[] configureCallbacks() {
        LOG.info("Configuring Callbacks");
        return new Callback[] {new CustomCallbackHandler.CustomCallback()};
    }
}
