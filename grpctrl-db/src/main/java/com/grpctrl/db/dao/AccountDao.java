package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;
import com.grpctrl.db.error.DaoException;

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
     * @param consumer the consumer to receive the indicated account objects
     * @param accountIds the unique identifiers of the accounts to be consumed
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(@Nonnull Consumer<Account> consumer, @Nonnull Long... accountIds) throws DaoException;

    /**
     * Consume the accounts with the specified unique ids.
     *
     * @param consumer the consumer to receive the indicated account objects
     * @param accountIds the unique identifiers of the accounts to be consumed
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void get(@Nonnull Consumer<Account> consumer, @Nonnull Collection<Long> accountIds) throws DaoException;

    /**
     * Consume all the accounts in the system.
     *
     * @param consumer the consumer to receive each of the available account objects
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void getAll(@Nonnull Consumer<Account> consumer) throws DaoException;

    /**
     * Add the specified accounts to the backing store.
     *
     * @param consumer the consumer to receive each of the stored account objects (which now include their unique
     *     identifiers)
     * @param accounts the collection of accounts to be added to the backing store
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void add(@Nonnull Consumer<Account> consumer, @Nonnull Account... accounts) throws DaoException;

    /**
     * Add the specified accounts to the backing store.
     *
     * @param consumer the consumer to receive each of the stored account objects (which now include their unique
     *     identifiers)
     * @param accounts the collection of accounts to be added to the backing store
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    void add(@Nonnull Consumer<Account> consumer, @Nonnull Collection<Account> accounts) throws DaoException;

    /**
     * Remove the accounts with the specified ids.
     *
     * @param accountIds the unique identifiers indicating the accounts to be deleted
     *
     * @return the total number of accounts removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int remove(@Nonnull Long... accountIds) throws DaoException;

    /**
     * Remove the accounts with the specified ids.
     *
     * @param accountIds the unique identifiers indicating the accounts to be deleted
     *
     * @return the total number of accounts removed
     *
     * @throws NullPointerException if the parameter is {@code null}
     * @throws DaoException if there is a problem interacting with the database
     */
    int remove(@Nonnull Collection<Long> accountIds) throws DaoException;
}
