package com.grpctrl.db.dao;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserRole;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage user role objects in the database.
 */
public interface UserRoleDao {
    /**
     * Retrieve the {@link UserRole} objects for the specified users.
     *
     * @param conn the {@link Connection} to use when retrieving the user role objects
     * @param userIds the unique identifiers of the users for which user role objects should be retrieved
     *
     * @return the requested {@link UserRole} objects, mapped by user id
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    Map<Long, Collection<UserRole>> get(@Nonnull Connection conn, @Nonnull Collection<Long> userIds);

    /**
     * Add the roles from the specified users to the database.
     *
     * @param conn the {@link Connection} to use when adding the user roles
     * @param users the {@link User} objects from which the roles will be pulled and inserted into the data store
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    void add(@Nonnull Connection conn, @Nonnull Collection<User> users);
}
