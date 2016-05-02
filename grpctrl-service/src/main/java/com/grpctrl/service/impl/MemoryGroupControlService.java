package com.grpctrl.service.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.service.GroupControlService;
import com.grpctrl.service.error.GroupExistsException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

/**
 * Provides an in-memory implementation of a {@link GroupControlService}.
 */
public class MemoryGroupControlService implements GroupControlService {
    @Nonnull
    private final ConcurrentHashMap<Account, ConcurrentHashMap<Long, Group>> accounts;

    private final AtomicLong groupIdCounter = new AtomicLong(0L);

    /**
     * Default constructor.
     */
    public MemoryGroupControlService() {
        this.accounts = new ConcurrentHashMap<>();
    }

    @Nonnull
    private ConcurrentHashMap<Account, ConcurrentHashMap<Long, Group>> getAccounts() {
        return this.accounts;
    }

    @Nonnull
    private ConcurrentHashMap<Long, Group> getAndCreateGroupMap(@Nonnull final Account account) {
        final ConcurrentHashMap<Long, Group> newmap = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Long, Group> oldmap = getAccounts().putIfAbsent(account, newmap);
        return (oldmap == null) ? newmap : oldmap;
    }

    @Nonnull
    private Optional<ConcurrentHashMap<Long, Group>> getGroupMap(
            @Nonnull final Account account, final boolean create) {
        if (create) {
            return Optional.of(getAndCreateGroupMap(account));
        } else {
            return Optional.ofNullable(getAccounts().get(account));
        }
    }

    @Nonnull
    private Optional<Group> getGroup(@Nonnull final Account account, @Nonnull final Long id) {
        final Optional<ConcurrentHashMap<Long, Group>> groupMap = getGroupMap(account, false);
        if (groupMap.isPresent()) {
            return Optional.ofNullable(groupMap.get().get(id));
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final Long id) {
        return getGroup(Objects.requireNonNull(account), Objects.requireNonNull(id)).isPresent();
    }

    @Override
    public void add(@Nonnull final Account account, @Nonnull final Group group) throws GroupExistsException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(group);

        final long groupId = this.groupIdCounter.incrementAndGet();
        // TODO: Need to update children with ids also.
        final Group withId = new Group.Builder(group).setId(groupId).build();

        final ConcurrentHashMap<Long, Group> groupMap = getAndCreateGroupMap(account);
        if (groupMap.putIfAbsent(groupId, withId) != null) {
            throw new GroupExistsException("A group with id " + groupId + " already exists");
        }
    }

    @Override
    public void remove(@Nonnull final Account account, @Nonnull final Long id) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(id);

        final Optional<ConcurrentHashMap<Long, Group>> groupMap = getGroupMap(account, false);
        if (groupMap.isPresent()) {
            groupMap.get().remove(id);
        }
    }
}
