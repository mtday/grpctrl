package com.grpctrl.run;

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
            if (isIgnored())
                return false;

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

    /*
    @Nullable
    private CallbackHandler callbackHandler;

    @Nullable
    private Subject subject;

    @Inject
    public CustomLoginModule() {
        LOG.info("constructor");
    }

    @Override
    public void initialize(
            @Nonnull final Subject subject, @Nonnull final CallbackHandler callbackHandler,
            @Nonnull final Map<String, ?> sharedState, @Nonnull final Map<String, ?> options) {
        LOG.info("initialize");
        LOG.info("  subject class: {}", subject.getClass().getName());
        LOG.info("  subject: {}", subject);
        LOG.info("  callback: {}", callbackHandler);
        LOG.info("  sharedState: {}", sharedState);
        LOG.info("  options: {}", options);

        this.callbackHandler = callbackHandler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {
        LOG.info("login");

        final RequestParameterCallback callback = new RequestParameterCallback();
        callback.setParameterName("code");

        if (this.callbackHandler != null) {
            try {
                this.callbackHandler.handle(new Callback[] {callback});
            } catch (final UnsupportedCallbackException unsupported) {
                LOG.error("Unsupported callback", unsupported);
            } catch (final IOException ioException) {
                LOG.error("Callback handler failed", ioException);
            }
        }

        ;
        LOG.info("Parameter Name is: {}", callback.getParameterName());
        LOG.info("Parameter Values are: {}", callback.getParameterValues());

        final List<?> values = callback.getParameterValues();
        if (values != null && !values.isEmpty()) {
            // Just expecting one code value.
            final String code = String.valueOf(values.iterator().next());

            if (this.subject != null) {
                this.subject.getPrincipals().add(new JAASPrincipal(code));
                this.subject.getPrincipals().add(new JAASPrincipal("ADMIN"));
                this.subject.getPrincipals().add(new JAASPrincipal("USER"));
                //this.subject.getPublicCredentials().add("ADMIN");
                //this.subject.getPublicCredentials().add("USER");
                //this.subject.getPrivateCredentials().add("ADMIN");
                //this.subject.getPrivateCredentials().add("USER");
            } else {
                throw new LoginException("No subject available");
            }
        } else {
            throw new LoginException("No authorization code available");
        }

        LOG.info("subject is now: {}", this.subject);
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        LOG.info("commit");
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        LOG.info("abort");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        LOG.info("logout");
        if (this.subject != null) {
            this.subject.getPrincipals().clear();
        }
        return true;
    }
    */
}
