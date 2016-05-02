package com.grpctrl.db;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.error.DaoException;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines the interface of the data access layer used to manage our objects in the database.
 */
public interface GroupControlDao {
    /**
     * Retrieve the account with the specified id.
     *
     * @param accountId the unique id of the account to be retrieved
     *
     * @return the account with the specified id, if available
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Optional<Account> getAccount(@Nonnull Long accountId) throws DaoException;

    /**
     * Add the specified account to the backing store.
     *
     * @param account the account to be added to the backing store
     *
     * @return the new account that was added to the backing store (including its unique identifier)
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Account addAccount(@Nonnull Account account) throws DaoException;

    /**
     * Remove the account with the specified id.
     *
     * @param accountId the unique id of the account to be deleted
     *
     * @return whether the account was removed (only returning false if the account did not exist)
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    boolean removeAccount(@Nonnull Long accountId) throws DaoException;

    /**
     * Retrieve the group with the specified id, but without looking for its child groups (tags will be
     * included, however).
     *
     * @param account the account for which groups will be retrieved
     * @param groupId the unique id of the group to be retrieved
     *
     * @return the group with the specified id (including its tags, but not its children), if available
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Optional<Group> getGroup(@Nonnull Account account, @Nonnull Long groupId) throws DaoException;

    /**
     * Retrieve the specified group, recursively finding and including its child groups and tags as well.
     *
     * @param account the account for which group information will be retrieved
     * @param groupId the unique id of the group to be retrieved
     *
     * @return the requested group (including its children and tags), if available
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Optional<Group> getRecursiveGroup(@Nonnull Account account, @Nonnull Long groupId)
            throws DaoException;

    /**
     * Retrieve all of the groups for an account, recursively finding groups and child groups.
     *
     * @param account the account for which groups will be retrieved
     *
     * @return a collection of all the groups for the provided account, possibly empty if no groups have been added
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Collection<Group> getRecursiveGroups(@Nonnull Account account) throws DaoException;

    /**
     * Add the specified groups to the backing store.
     *
     * @param account the account that owns the groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @return the newly stored group objects (including their unique identifiers)
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Collection<Group> addGroups(@Nonnull Account account, @Nonnull Collection<Group> groups) throws DaoException;

    /**
     * Add the specified groups to the backing store as children for the specified parent id.
     *
     * @param account the account that owns the groups
     * @param parentId the unique identifier of the parent group into which the provides groups will be added, possibly
     *     {@code null} in which case the new groups will be top-level groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @return the newly stored group objects (including their unique identifiers)
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    @Nonnull
    Collection<Group> addGroups(@Nonnull Account account, @Nullable Long parentId, @Nonnull Collection<Group> groups)
            throws DaoException;

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
    int removeGroups(@Nonnull Account account, @Nonnull Collection<Long> groupIds)
            throws DaoException;

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
    int addTags(
            @Nonnull Account account, @Nonnull Long groupId, @Nonnull Collection<Tag> tags)
            throws DaoException;

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
    int removeTags(
            @Nonnull Account account, @Nonnull Long groupId, @Nonnull Collection<Tag> tags)
            throws DaoException;

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
    int removeTagLabels(
            @Nonnull Account account, @Nonnull Long groupId,
            @Nonnull Collection<String> tagLabels) throws DaoException;
}
