package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.db.dao.ServiceLevelDao;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Provides an implementation of a {@link ServiceLevelDao}.
 */
public class PostgresServiceLevelDao implements ServiceLevelDao {
    @Override
    public void add(@Nonnull final Connection conn, @Nonnull final Collection<Account> accounts)
            throws DaoException {
        // Note that the collection of accounts are expected to be already in a reasonable batch size, since they are
        // coming from the PostgresAccountDao already in a batch.

        final String SQL =
                "INSERT INTO service_levels (account_id, max_groups, max_tags, max_depth) VALUES (?, ?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            for (final Account account : accounts) {
                ps.setLong(1, account.getId().orElse(null));
                ps.setInt(2, account.getServiceLevel().getMaxGroups());
                ps.setInt(3, account.getServiceLevel().getMaxTags());
                ps.setInt(4, account.getServiceLevel().getMaxDepth());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add service levels", sqlException);
        }
    }
}
