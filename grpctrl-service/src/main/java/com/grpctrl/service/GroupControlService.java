package com.grpctrl.service;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.service.error.GroupExistsException;

/**
 * Defines the service interface.
 */
public interface GroupControlService {
    /**
     * Determine if a group with the specified unique id already exists.
     *
     * @param account the distinct account on which the service operation applies
     * @param id the unique identifier indicating the group to check for existence
     *
     * @return whether the specified group id exists in the provided account
     */
    boolean exists(Account account, String id);

    /**
     * Add a new group to the underlying service.
     *
     * @param account the distinct account on which the service operation applies
     * @param group the new group to be added
     *
     * @throws GroupExistsException if a group with the same unique identifier already exists
     */
    void add(Account account, Group group) throws GroupExistsException;

    /**
     * Add a new group to the underlying service.
     *
     * @param account the distinct account on which the service operation applies
     * @param id the unique identifier indicating the group to be removed
     */
    void remove(Account account, String id);
}
