package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage our objects in the database.
 */
public interface TagDao {
    /**
     * Retrieve a count of the number of tags owned by an account.
     *
     * @param conn the {@link Connection} to use when retrieving the tag count as part of an existing transaction
     * @param account the account for which tags are to be counted
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int count(@Nonnull Connection conn, @Nonnull Account account) throws DaoException;

    /**
     * Retrieve all of the tags associated with the specified groups.
     *
     * @param consumer the consumer used to process each of the groups with their tags
     * @param conn the {@link Connection} to use when retrieving the tags as part of an existing transaction
     * @param account the account that owns the specified groups
     * @param groups the groups for which tags are to be retrieved
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Connection conn, @Nonnull Account account,
            @Nonnull Collection<Group> groups) throws DaoException;

    /**
     * Add all the tags contained in the provided groups.
     *
     * @param consumer the consumer used to process each of the groups with their tags after addition
     * @param conn the {@link Connection} to use when adding the tags as part of an existing transaction
     * @param account the account that owns the group
     * @param groups the groups containing the tags to be inserted
     *
     * @return the number of tags that were inserted into the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int add(
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull Connection conn, @Nonnull Account account,
            @Nonnull Collection<Group> groups) throws DaoException;

    /**
     * Add the specified tags to the group with the provided id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group to which the tag will be assigned
     * @param tags the collection of tags to be assigned to the group
     *
     * @return the number of tags that were inserted into the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int add(@Nonnull Account account, @Nonnull Long groupId, @Nonnull Collection<Tag> tags) throws DaoException;

    /**
     * Remove the specified tags from the group with the specified id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group from which the tag will be removed
     * @param tags the collection tags to be removed from the group
     *
     * @return the number of tags removed from the backing store, will only be smaller than the size of the provided
     *     collection when some of the tags were not found to delete
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int remove(@Nonnull Account account, @Nonnull Long groupId, @Nonnull Collection<Tag> tags) throws DaoException;

    /**
     * Remove tags with the specified labels from the group with the provided id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group from which the tags will be removed
     * @param tagLabels the collection of tag labels to be removed from the group (with corresponding values)
     *
     * @return the number of tags removed from the backing store, will only be smaller than the size of the provided
     *     collection when some of the tags were not found to delete, and may be larger than the size of the collection
     *     if some of the tag labels had multiple distinct values
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int removeLabels(@Nonnull Account account, @Nonnull Long groupId, @Nonnull Collection<String> tagLabels)
            throws DaoException;
}
