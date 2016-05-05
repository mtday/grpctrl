package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.util.Iterator;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines the interface of the data access layer used to manage groups in the database.
 */
public interface GroupDao {
    /**
     * Retrieve a count of the number of groups owned by an account.
     *
     * @param conn the {@link Connection} to use when retrieving the group count as part of an existing transaction
     * @param account the account for which groups are to be counted
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int count(@Nonnull Connection conn, @Nonnull Account account) throws DaoException;

    /**
     * Check to see if the group with the specified name exists in the data store.
     *
     * @param account the account to check for the existence of the group
     * @param groupName the name of the group to check for existence
     *
     * @return whether any groups exist with the specified name
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    boolean exists(@Nonnull Account account, @Nonnull String groupName) throws DaoException;

    /**
     * Retrieve the top-level groups.
     *
     * @param consumer the consumer to which the identified groups and tags will be passed
     * @param account the account for which groups will be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(@Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account) throws DaoException;

    /**
     * Retrieve the group with the specified id.
     *
     * @param consumer the consumer to which the identified group and tags will be passed, if found
     * @param account the account for which groups will be retrieved
     * @param groupId the unique id of the group to be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account, @Nonnull Long groupId)
            throws DaoException;

    /**
     * Retrieve the groups with the specified name.
     *
     * @param consumer the consumer to which the identified groups and tags will be passed
     * @param account the account for which groups will be retrieved
     * @param groupName the name of the groups to be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(@Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account, @Nonnull String groupName)
            throws DaoException;

    /**
     * Retrieve the top-level groups (this with parent id being null).
     *
     * @param consumer the consumer to which the identified groups and tags will be passed
     * @param account the account for which group information will be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void children(@Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account) throws DaoException;

    /**
     * Retrieve the children of the group with the specified id.
     *
     * @param consumer the consumer to which the identified groups and tags will be passed
     * @param account the account for which group information will be retrieved
     * @param parentId the unique id of the group for which children will be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void children(@Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account, @Nonnull Long parentId)
            throws DaoException;

    /**
     * Retrieve the children of the group with the specified name.
     *
     * @param consumer the consumer to which the identified groups will be passed
     * @param account the account for which group information will be retrieved
     * @param parentName the name of the parent groups for which children will be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void children(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account, @Nonnull String parentName)
            throws DaoException;

    /**
     * Add the specified groups to the backing store.
     *
     * @param consumer the consumer to which all the inserted groups and tags will be passed, including their unique
     *     identifiers
     * @param account the account that owns the groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void add(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account,
            @Nonnull Iterable<Group> groups) throws DaoException;

    /**
     * Add the specified groups to the backing store as children for the specified parent id.
     *
     * @param consumer the consumer to which all the inserted groups and tags will be passed, including their unique
     *     identifiers
     * @param account the account that owns the groups
     * @param parentId the unique identifier of the parent group into which the provides groups will be added, possibly
     *     {@code null} in which case the new groups will be top-level groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void add(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Account account, @Nullable Long parentId,
            @Nonnull Iterable<Group> groups) throws DaoException;

    /**
     * Remove groups with the specified unique identifiers.
     *
     * @param account the account that owns the groups
     * @param groupIds the collection of identifiers indicating which groups are to be removed
     *
     * @return the number of groups removed from the backing store, will only be smaller than the size of the provided
     *     id collection when some of the ids were not found to delete
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int remove(@Nonnull Account account, @Nonnull Iterable<Long> groupIds) throws DaoException;
}
