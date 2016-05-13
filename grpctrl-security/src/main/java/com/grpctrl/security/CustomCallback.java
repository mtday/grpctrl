package com.grpctrl.security;

import com.google.common.collect.Sets;
import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.supplier.ExecutorServiceSupplier;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.callback.Callback;

public class CustomCallback implements Callback {
    private static final Logger LOG = LoggerFactory.getLogger(CustomCallback.class);

    @Nullable
    private User user;

    public void run(
            @Nonnull final ExecutorServiceSupplier executorServiceSupplier,
            @Nonnull final UserDaoSupplier userDaoSupplier, @Nonnull final User user,
            @Nonnull final Collection<Callable<Void>> callables) throws IOException {
        final CompletionService<Void> completionService =
                new ExecutorCompletionService<>(Objects.requireNonNull(executorServiceSupplier).get());
        this.user = Objects.requireNonNull(user);

        final Collection<Future<Void>> futures =
                callables.stream().map(completionService::submit).collect(Collectors.toList());

        // Wait for all of the futures to complete successfully.
        while (!futures.isEmpty()) {
            try {
                final Future<Void> completedFuture = completionService.take();
                futures.remove(completedFuture);
                completedFuture.get();
            } catch (final ExecutionException | InterruptedException problem) {
                for (final Future<Void> stillRunning : futures) {
                    stillRunning.cancel(true);
                }
                throw new IOException("Failed to authenticate user", problem);
            }
        }

        // Now that we have a populated user, see if we have a matching user in the database.
        final Optional<User> fromDb = userDaoSupplier.get().get(user.getUserSource(), user.getLogin());
        if (fromDb.isPresent()) {
            LOG.info("Found existing user in DB: {}", fromDb.get());
            user.setId(fromDb.get().getId().orElse(null));
            user.setRoles(fromDb.get().getRoles());
            user.setAccounts(fromDb.get().getAccounts());
            user.setUserAuth(fromDb.get().getUserAuth().orElse(null));
            user.setEmails(Sets.union(user.getEmails(), fromDb.get().getEmails()));

            // TODO: persist this updated user (to capture email changes)?
            LOG.info("After merger: {}", user);
        } else {
            LOG.info("Not found in DB, adding user: {}", user);
            // Add this new user to the database.
            user.setRoles(Collections.singleton(UserRole.USER));
            userDaoSupplier.get().add(user);
        }
    }

    @Nullable
    public User getUser() {
        return this.user;
    }
}
