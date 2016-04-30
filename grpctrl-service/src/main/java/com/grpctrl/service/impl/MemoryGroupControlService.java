package com.grpctrl.service.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.service.GroupControlService;
import com.grpctrl.service.error.GroupExistsException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

/**
 * Provides an in-memory implementation of a {@link GroupControlService}.
 */
public class MemoryGroupControlService implements GroupControlService {
    @Nonnull
    private final ConcurrentHashMap<Account, ConcurrentHashMap<String, Group>> accounts;

    /**
     * Default constructor.
     */
    public MemoryGroupControlService() {
        this.accounts = new ConcurrentHashMap<>();
    }

    private ConcurrentHashMap<Account, ConcurrentHashMap<String, Group>> getAccounts() {
        return this.accounts;
    }

    private ConcurrentHashMap<String, Group> getAndCreateGroupMap(@Nonnull final Account account) {
        final ConcurrentHashMap<String, Group> newmap = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Group> oldmap = getAccounts().putIfAbsent(account, newmap);
        return (oldmap == null) ? newmap : oldmap;
    }

    private Optional<ConcurrentHashMap<String, Group>> getGroupMap(
            @Nonnull final Account account, final boolean create) {
        if (create) {
            return Optional.of(getAndCreateGroupMap(account));
        } else {
            return Optional.ofNullable(getAccounts().get(account));
        }
    }

    private Optional<Group> getGroup(@Nonnull final Account account, @Nonnull final String id) {
        final Optional<ConcurrentHashMap<String, Group>> groupMap = getGroupMap(account, false);
        if (groupMap.isPresent()) {
            return Optional.ofNullable(groupMap.get().get(id));
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final String id) {
        return getGroup(Objects.requireNonNull(account), Objects.requireNonNull(id)).isPresent();
    }

    @Override
    public void add(@Nonnull final Account account, @Nonnull final Group group) throws GroupExistsException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(group);

        final ConcurrentHashMap<String, Group> groupMap = getAndCreateGroupMap(account);
        if (groupMap.putIfAbsent(group.getId(), group) != null) {
            throw new GroupExistsException("A group with id " + group.getId() + " already exists");
        }
    }

    @Override
    public void remove(@Nonnull final Account account, @Nonnull final String id) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(id);

        final Optional<ConcurrentHashMap<String, Group>> groupMap = getGroupMap(account, false);
        if (groupMap.isPresent()) {
            groupMap.get().remove(id);
        }
    }
}
