package com.grpctrl.security;

import com.grpctrl.common.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class CustomLoginModule implements LoginModule {
    private static final Logger LOG = LoggerFactory.getLogger(CustomLoginModule.class);

    private boolean authenticated = false;
    private boolean committed = false;
    private User currentUser;
    private Subject subject;
    private CallbackHandler callbackHandler;

    @Override
    public void initialize(@Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler,
            @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        this.subject = Objects.requireNonNull(subject);
        this.callbackHandler = Objects.requireNonNull(callbackHandler);
    }

    @Override
    public boolean abort() throws LoginException {
        this.currentUser = null;
        return this.authenticated && this.committed;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!this.authenticated) {
            this.currentUser = null;
            this.committed = false;
            return false;
        }

        this.committed = true;
        return true;
    }

    public boolean login() throws LoginException {
        try {
            final CustomCallback customCallback = new CustomCallback();
            final Callback[] callbacks = {customCallback};
            this.callbackHandler.handle(callbacks);

            this.currentUser = customCallback.getUser();

            if (this.currentUser == null) {
                this.authenticated = false;
                throw new FailedLoginException("User is null");
            }

            this.subject.getPrincipals().add(this.currentUser);
            this.subject.getPrincipals().addAll(this.currentUser.getRoles());

            this.authenticated = true;
            return true;
        } catch (final Exception exception) {
            LOG.error("Failed to perform login", exception);
            throw new LoginException("Failed to perform login");
        }
    }

    public boolean logout() throws LoginException {
        this.subject.getPrincipals().remove(this.currentUser);
        this.subject.getPrincipals().removeAll(this.currentUser.getRoles());
        return true;
    }
}
