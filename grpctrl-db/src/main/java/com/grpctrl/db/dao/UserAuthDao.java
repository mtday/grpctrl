package com.grpctrl.db.dao;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserAuth;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage user authorization objects in the database.
 */
public interface UserAuthDao {
    /**
     * Retrieve the {@link UserAuth} objects for the specified users.
     *
     * @param conn the {@link Connection} to use when retrieving the user authorization objects
     * @param userIds the unique identifiers of the users for which user authorization objects should be retrieved
     *
     * @return the requested {@link UserAuth} objects, mapped by user id
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    Map<Long, UserAuth> get(@Nonnull Connection conn, @Nonnull Collection<Long> userIds);

    /**
     * Add the user auth information for the specified users to the database.
     *
     * @param conn the {@link Connection} to use when adding the user authorizations
     * @param users the {@link User} objects from which the auth information will be pulled and inserted into the data
     * store
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    void add(@Nonnull Connection conn, @Nonnull Collection<User> users);
}
