package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage account objects in the database.
 */
public interface AccountDao {
    /**
     * Consume the account with the specified unique id.
     *
     * @param accountId the unique identifier of the account to be consumed
     * @param consumer the consumer to receive the indicated account object
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void get(@Nonnull Long accountId, @Nonnull Consumer<Account> consumer);

    /**
     * Consume the accounts with the specified unique ids.
     *
     * @param accountIds the unique identifiers of the accounts to be consumed
     * @param consumer the consumer to receive the indicated account objects
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void get(@Nonnull Collection<Long> accountIds, @Nonnull Consumer<Account> consumer);

    /**
     * Consume all the accounts available for the specified user id.
     *
     * @param userId the unique identifier of the user for which accounts should be consumed
     *
     * @return the collection of accounts available for the specified user
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    Collection<Account> getForUser(@Nonnull Long userId);

    /**
     * Retrieve all of the accounts available for the specified user ids.
     *
     * @param conn the {@link Connection} used to retrieve the accounts
     * @param userIds the unique identifiers of the users for which accounts should be consumed
     *
     * @return the collection of accounts available for the specified user
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    Map<Long, Collection<Account>> getForUsers(@Nonnull Connection conn, @Nonnull Collection<Long> userIds);

    /**
     * Consume all the accounts in the system.
     *
     * @param consumer the consumer to receive each of the available account objects
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void getAll(@Nonnull Consumer<Account> consumer);

    /**
     * Add the specified accounts to the backing store.
     *
     * @param accounts the iterable of accounts to be added to the backing store
     * @param consumer the consumer to receive each of the stored account objects (which now include their unique
     *     identifiers)
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    void add(@Nonnull Iterator<Account> accounts, @Nonnull Consumer<Account> consumer);

    /**
     * Remove the account with the specified id.
     *
     * @param accountId the unique identifier indicating the account to be deleted
     *
     * @return the total number of accounts removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    int remove(@Nonnull Long accountId);

    /**
     * Remove the accounts with the specified ids.
     *
     * @param accountIds the unique identifiers indicating the accounts to be deleted
     *
     * @return the total number of accounts removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws javax.ws.rs.WebApplicationException if there is a problem interacting with the database
     */
    int remove(@Nonnull Collection<Long> accountIds);
}
