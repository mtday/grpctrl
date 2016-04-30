package com.grpctrl.service.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.service.GroupControlService;
import com.grpctrl.service.error.GroupExistsException;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Provides a database-backed implementation of a {@link GroupControlService}.
 */
public class DatabaseGroupControlService implements GroupControlService {
    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final String id) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(id);

        // TODO
        return false;
    }

    @Override
    public void add(@Nonnull final Account account, @Nonnull final Group group) throws GroupExistsException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(group);

        // TODO
    }

    @Override
    public void remove(@Nonnull final Account account, @Nonnull final String id) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(id);

        // TODO
    }
}
