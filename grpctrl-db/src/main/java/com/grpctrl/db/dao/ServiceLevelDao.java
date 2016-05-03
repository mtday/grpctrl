package com.grpctrl.db.dao;

import com.grpctrl.common.model.Account;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage service level objects in the database.
 */
public interface ServiceLevelDao {
    /**
     * Add the service levels contained in the specified account objects to the data store.
     *
     * @param conn the {@link Connection} to use when adding the service levels to maintain the current transaction
     * @param accounts the account objects containing the service levels to be added to the data store
     *
     * @throws DaoException if there is a problem interacting with the database
     */
    void add(@Nonnull Connection conn, @Nonnull Collection<Account> accounts) throws DaoException;
}
