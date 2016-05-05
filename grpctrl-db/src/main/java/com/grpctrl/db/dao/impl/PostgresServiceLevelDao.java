package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.dao.ServiceLevelDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;

/**
 * Provides an implementation of a {@link ServiceLevelDao}.
 */
public class PostgresServiceLevelDao implements ServiceLevelDao {
    @Override
    public CloseableBiConsumer<Long, ServiceLevel> getAddConsumer(@Nonnull final Connection conn) {
        return new AddConsumer(conn);
    }

    private static class AddConsumer implements CloseableBiConsumer<Long, ServiceLevel> {
        private static final String SQL =
                "INSERT INTO service_levels (account_id, max_groups, max_tags, max_depth) VALUES (?, ?, ?, ?)";

        private final PreparedStatement ps;

        public AddConsumer(@Nonnull final Connection conn) {
            try {
                this.ps = Objects.requireNonNull(conn).prepareStatement(SQL);
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException(
                        "Failed to create service level prepared statement", sqlException);
            }
        }

        @Override
        public void accept(@Nonnull final Long accountId, @Nonnull final ServiceLevel serviceLevel) {
            try {
                this.ps.setLong(1, accountId);
                this.ps.setInt(2, serviceLevel.getMaxGroups());
                this.ps.setInt(3, serviceLevel.getMaxTags());
                this.ps.setInt(4, serviceLevel.getMaxDepth());
                this.ps.addBatch();
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException("Failed to add service level batch", sqlException);
            }
        }

        @Override
        public void close() {
            try {
                this.ps.executeBatch();
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException("Failed to execute service level batch", sqlException);
            } finally {
                try {
                    this.ps.close();
                } catch (final SQLException sqlException) {
                    throw new InternalServerErrorException("Failed to close prepared statement", sqlException);
                }
            }
        }
    }
}
