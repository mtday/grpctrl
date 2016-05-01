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
    Optional<Account> getAccount(@Nonnull String accountId) throws DaoException;

    /**
     * Add the specified account to the backing store.
     *
     * @param account the account to be added to the backing store
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void addAccount(@Nonnull Account account) throws DaoException;

    /**
     * Remove the account with the specified id.
     *
     * @param accountId the unique id of the account to be deleted
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void removeAccount(@Nonnull String accountId) throws DaoException;

    /**
     * Retrieve the top-level group with the specified id.
     *
     * @param account the account for which groups will be retrieved
     * @param groupId the unique id of the top-level group to be retrieved
     *
     * @return the group with the specified id, if available
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    Optional<Group> getGroup(@Nonnull Account account, @Nonnull String groupId) throws DaoException;

    /**
     * Retrieve the group with the specified id residing within the specified parent group.
     *
     * @param account the account for which groups will be retrieved
     * @param parentId the unique id of the parent group for which a group should be retrieved, possibly {@code null}
     *     in which case the top-level group with the specified id should be returned
     * @param groupId the unique id of the group to be retrieved
     *
     * @return the group with the specified id within the parent group, if available
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    Optional<Group> getGroup(@Nonnull Account account, @Nullable String parentId, @Nonnull String groupId)
            throws DaoException;

    /**
     * Retrieve all of the groups for an account.
     *
     * @param account the account for which groups will be retrieved
     *
     * @return a collection of all the groups for the provided account
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    Collection<Group> getGroups(@Nonnull Account account) throws DaoException;

    /**
     * Add the specified groups to the backing store.
     *
     * @param account the account that owns the groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void addGroups(@Nonnull Account account, @Nonnull Collection<Group> groups) throws DaoException;

    /**
     * Add the specified groups to the backing store as children for the specified parent id.
     *
     * @param account the account that owns the groups
     * @param parentId the unique identifier of the parent group into which the provides groups will be added, possibly
     *     {@code null} in which case the new groups will be top-level groups
     * @param groups the collection of groups to be added to the backing store
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void addGroups(@Nonnull Account account, @Nullable String parentId, @Nonnull Collection<Group> groups)
            throws DaoException;

    /**
     * Remove top-level groups with the specified unique identifiers.
     *
     * @param account the account that owns the groups
     * @param groupIds the collection of top-level group identifiers indicating which groups are to be removed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void removeGroups(@Nonnull Account account, @Nonnull Collection<String> groupIds) throws DaoException;

    /**
     * Remove groups with the specified unique identifiers where they exist as children to the specified parent.
     *
     * @param account the account that owns the groups
     * @param parentId the unique identifier of the parent group from which the children groups will be removed,
     *     possibly {@code null} in which case the top-level groups with the specified ids will be removed
     * @param groupIds the collection of identifiers indicating which groups are to be removed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void removeGroups(@Nonnull Account account, @Nullable String parentId, @Nonnull Collection<String> groupIds)
            throws DaoException;

    /**
     * Add the specified tags to the group with the provided id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group to which the tag will be assigned
     * @param tags the collection of tags to be assigned to the group
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void addTags(@Nonnull Account account, @Nonnull String groupId, @Nonnull Collection<Tag> tags) throws DaoException;

    /**
     * Remove the specified tags from the group with the specified id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group from which the tag will be removed
     * @param tags the collection tags to be removed from the group
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void removeTags(@Nonnull Account account, @Nonnull String groupId, @Nonnull Collection<Tag> tags)
            throws DaoException;

    /**
     * Remove tags with the specified labels from the group with the provided id.
     *
     * @param account the account that owns the group
     * @param groupId the unique identifier of the group from which the tags will be removed
     * @param tagLabels the collection of tag labels to be removed from the group (with corresponding values)
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void removeTagLabels(@Nonnull Account account, @Nonnull String groupId, @Nonnull Collection<String> tagLabels)
            throws DaoException;
}
