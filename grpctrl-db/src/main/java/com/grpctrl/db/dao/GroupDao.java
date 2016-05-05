package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;

import java.sql.Connection;
import java.util.Collection;
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
     * @return the number of groups owned by the account
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    int count(@Nonnull Connection conn, @Nonnull Account account);

    /**
     * Determine the depth of the group with the specified id.
     *
     * @param conn the {@link Connection} to use when retrieving the group count as part of an existing transaction
     * @param account the account to check for the depth of the group
     * @param groupId the unique identifier of the group to check for depth
     *
     * @return the depth of the group with the specified id, or -1 if the group id does not exist
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    int depth(@Nonnull Connection conn, @Nonnull Account account, @Nonnull Long groupId);

    /**
     * Check to see if the group with the specified id exists in the data store.
     *
     * @param account the account to check for the existence of the group
     * @param groupId the unique identifier of the group to check for existence
     *
     * @return whether any groups exist with the specified id
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    boolean exists(@Nonnull Account account, @Nonnull Long groupId);

    /**
     * Check to see if the group with the specified name exists in the data store.
     *
     * @param account the account to check for the existence of the group
     * @param groupName the name of the group to check for existence
     *
     * @return whether any groups exist with the specified name
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    boolean exists(@Nonnull Account account, @Nonnull String groupName);

    /**
     * Retrieve the top-level groups.
     *
     * @param account the account for which groups will be retrieved
     * @param consumer the consumer to which the identified groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void get(@Nonnull Account account, @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the groups with the specified unique identifiers.
     *
     * @param account the account for which groups will be retrieved
     * @param groupIds the unique id of the group to be retrieved
     * @param consumer the consumer to which the identified group and tags will be passed, if found
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void getById(
            @Nonnull Account account, @Nonnull Collection<Long> groupIds,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the groups with the specified names.
     *
     * @param account the account for which groups will be retrieved
     * @param groupNames the names of the groups to be retrieved
     * @param consumer the consumer to which the identified groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void getByName(
            @Nonnull Account account, @Nonnull Collection<String> groupNames,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the groups with the names matching the provided POSIX regular expression values.
     *
     * @param account the account for which groups will be retrieved
     * @param regexes the POSIX regular expressions to use when finding groups
     * @param caseSensitive whether the regular expressions should be processed with matching character case
     * @param consumer the consumer to which the identified groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void find(
            @Nonnull Account account, @Nonnull Collection<String> regexes, boolean caseSensitive,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the children of the group with the specified id.
     *
     * @param account the account for which group information will be retrieved
     * @param parentIds the unique ids of the groups for which children will be retrieved
     * @param consumer the consumer to which the identified groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void childrenById(
            @Nonnull Account account, @Nonnull Collection<Long> parentIds,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the children of the groups with the specified name.
     *
     * @param account the account for which group information will be retrieved
     * @param parentNames the names of the parent groups for which children will be retrieved
     * @param consumer the consumer to which the identified groups will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void childrenByName(
            @Nonnull Account account, @Nonnull Collection<String> parentNames,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Retrieve the children of the group with names matching the provided POSIX regular expressions.
     *
     * @param account the account for which group information will be retrieved
     * @param regexes the POSIX regular expressions to use when finding groups
     * @param caseSensitive whether the regular expressions should be processed with matching character case
     * @param consumer the consumer to which the identified groups will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void childrenFind(
            @Nonnull Account account, @Nonnull Collection<String> regexes, boolean caseSensitive,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Add the specified groups to the backing store.
     *
     * @param account the account that owns the groups
     * @param groups the collection of groups to be added to the backing store
     * @param consumer the consumer to which all the inserted groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void add(
            @Nonnull Account account, @Nonnull Iterable<Group> groups,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

    /**
     * Add the specified groups to the backing store as children for the specified parent id.
     *
     * @param account the account that owns the groups
     * @param parentId the unique identifier of the parent group into which the provides groups will be added, possibly
     *     {@code null} in which case the new groups will be top-level groups
     * @param groups the collection of groups to be added to the backing store
     * @param consumer the consumer to which all the inserted groups and tags will be passed
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void add(
            @Nonnull Account account, @Nullable Long parentId, @Nonnull Iterable<Group> groups,
            @Nonnull BiConsumer<Group, Iterator<Tag>> consumer);

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
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    int remove(@Nonnull Account account, @Nonnull Collection<Long> groupIds);
}
