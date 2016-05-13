package com.grpctrl.security;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserSource;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.ExecutorServiceSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;
import com.grpctrl.security.tasks.EmailLookup;
import com.grpctrl.security.tasks.LoginLookup;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.ServletRequest;

public class CustomCallbackHandler implements CallbackHandler {
    @Nonnull
    private final ExecutorServiceSupplier executorServiceSupplier;
    @Nonnull
    private final UserDaoSupplier userDaoSupplier;

    @Nonnull
    private final Collection<Callable<Void>> callables;

    @Nonnull
    private final User user = new User();

    public CustomCallbackHandler(
            @Nonnull final ExecutorServiceSupplier executorServiceSupplier, @Nonnull final ConfigSupplier config,
            @Nonnull final ObjectMapperSupplier objectMapper, @Nonnull final OAuth20ServiceSupplier oauth,
            @Nonnull final UserDaoSupplier userDaoSupplier, @Nonnull final ServletRequest servletRequest) {
        this.executorServiceSupplier = Objects.requireNonNull(executorServiceSupplier);
        this.userDaoSupplier = Objects.requireNonNull(userDaoSupplier);
        final String code = servletRequest.getParameter("code");

        this.callables = new LinkedList<>();
        if (code != null) {
            this.user.setUserSource(UserSource.GITHUB);
            this.callables.add(new LoginLookup(config, objectMapper, oauth, code, this.user));
            this.callables.add(new EmailLookup(config, objectMapper, oauth, code, this.user));
        } else {
            this.user.setUserSource(UserSource.LOCAL);
            // TODO: Is this an API login?
        }
    }

    @Override
    public void handle(@Nonnull final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof CustomCallback) {
                // The callables will update the user object as appropriate.
                ((CustomCallback) callback)
                        .run(this.executorServiceSupplier, this.userDaoSupplier, this.user, this.callables);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    @Nonnull
    public User getUser() {
        return this.user;
    }
}
