package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage account objects in the database.
 */
public interface AccountDao {
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
    void add(@Nonnull Iterable<Account> accounts, @Nonnull Consumer<Account> consumer);

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
