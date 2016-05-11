package com.grpctrl.db.dao;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserSource;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage user objects in the database.
 */
public interface UserDao {
    /**
     * Retrieve the {@link User} objects with the specified unique identifier.
     *
     * @param userId the unique identifier of the user to be retrieved
     *
     * @return the requested {@link User} object, if available
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    Optional<User> get(@Nonnull Long userId);

    /**
     * Retrieve all of the {@link User} objects with the specified unique identifiers.
     *
     * @param userIds the unique identifiers of the users to be retrieved
     *
     * @return the requested {@link User} objects
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    Collection<User> get(@Nonnull Collection<Long> userIds);

    /**
     * Retrieve the {@link User} object with the specified login.
     *
     * @param source the source of the user accounts to retrieve
     * @param login the unique user login value of the user to be retrieved
     *
     * @return the requested {@link User} object, if available
     *
     * @throws NullPointerException if either parameter is {@code null}
     */
    Optional<User> get(@Nonnull UserSource source, @Nonnull String login);

    /**
     * Retrieve all of the {@link User} objects with the specified logins.
     *
     * @param source the source of the user accounts to retrieve
     * @param logins the unique user login values of the users to be retrieved
     *
     * @return the requested {@link User} objects
     *
     * @throws NullPointerException if either parameter is {@code null}
     */
    Collection<User> get(@Nonnull UserSource source, @Nonnull Collection<String> logins);

    /**
     * Add the specified user account to the database
     *
     * @param user the {@link User} object to insert into the data store
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    void add(@Nonnull User user);

    /**
     * Add the specified user accounts to the database
     *
     * @param users the {@link User} objects to insert into the data store
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    void add(@Nonnull Collection<User> users);

    /**
     * Delete the {@link User} object with the specified unique identifier.
     *
     * @param userId the unique identifier of the user to be removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    void remove(@Nonnull Long userId);

    /**
     * Delete all of the {@link User} objects with the specified unique identifiers.
     *
     * @param userIds the unique identifiers of the users to be removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    void remove(@Nonnull Collection<Long> userIds);
}
