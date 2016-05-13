package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Tag;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.error.ErrorTransformer;
import com.grpctrl.db.error.QuotaExceededException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link TagDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database.
 */
@SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
public class PostgresTagDao implements TagDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     */
    public PostgresTagDao(@Nonnull final DataSourceSupplier dataSourceSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
    }

    @Override
    public int count(@Nonnull final Connection conn, @Nonnull final Account account) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);

        final String sql = "SELECT COUNT(*) FROM tags WHERE account_id = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve tag count", sqlException);
        }
    }

    @Override
    public CloseableBiConsumer<Long, Tag> getAddConsumer(@Nonnull Connection conn, @Nonnull Account account) {
        final int available = account.getServiceLevel().getMaxTags() - count(conn, account);
        return new AddConsumer(conn, account, available);
    }

    @Override
    public int add(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Iterable<Tag> tags) {
        final String sql = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        try {
            return processTags(sql, 1000, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to add tags", sqlException);
        }
    }

    @Override
    public int remove(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Iterable<Tag> tags) {
        final String sql = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ? AND tag_value = ?";

        try {
            return processTags(sql, 1000, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to remove tags", sqlException);
        }
    }

    private int processTags(
            @Nonnull final String sql, final int batchSize, @Nonnull final Account account, @Nonnull final Long groupId,
            @Nonnull final Iterable<Tag> tags) throws SQLException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tags);

        int modified = 0;

        final DataSource dataSource = this.dataSourceSupplier.get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            for (final Tag tag : tags) {
                ps.setString(3, tag.getLabel());
                ps.setString(4, tag.getValue());
                ps.addBatch();
                batches++;

                if (batches > batchSize) {
                    batches = 0;
                    modified += IntStream.of(ps.executeBatch()).sum();
                }
            }
            if (batches > 0) {
                modified += IntStream.of(ps.executeBatch()).sum();
            }
            conn.commit();
        }

        return modified;
    }

    @Override
    public int removeLabels(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Iterable<String> tagLabels) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tagLabels);

        int removed = 0;
        final int batchSize = 1000;
        final String sql = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ?";

        final DataSource dataSource = this.dataSourceSupplier.get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            for (final String tagLabel : tagLabels) {
                ps.setString(3, tagLabel);
                ps.addBatch();
                batches++;

                if (batches >= batchSize) {
                    batches = 0;
                    removed += IntStream.of(ps.executeBatch()).sum();
                }
            }
            if (batches > 0) {
                removed += IntStream.of(ps.executeBatch()).sum();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to remove tags by label", sqlException);
        }

        return removed;
    }

    private static class AddConsumer implements CloseableBiConsumer<Long, Tag> {
        private static final int BATCH_SIZE = 1000;
        private static final String SQL =
                "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        private final PreparedStatement ps;
        private final Account account;

        private final int available;
        private int added = 0;

        private int batchCount = 0;

        public AddConsumer(@Nonnull final Connection conn, @Nonnull final Account account, final int available) {
            try {
                this.ps = Objects.requireNonNull(conn).prepareStatement(SQL);
                this.account = Objects.requireNonNull(account);
                this.available = available;
            } catch (final SQLException sqlException) {
                throw ErrorTransformer.get("Failed to create tag insert prepared statement", sqlException);
            }
        }

        @Override
        public void accept(@Nonnull final Long groupId, @Nonnull final Tag tag) {
            try {
                this.ps.setLong(1, this.account.getId().orElse(null));
                this.ps.setLong(2, groupId);
                this.ps.setString(3, tag.getLabel());
                this.ps.setString(4, tag.getValue());
                this.ps.addBatch();
                this.batchCount++;

                if (this.batchCount >= BATCH_SIZE) {
                    this.ps.executeBatch();
                    this.batchCount = 0;
                    this.added += IntStream.of(this.ps.executeBatch()).sum();
                    if (this.available - this.added < 0) {
                        throw new QuotaExceededException(
                                "Unable to add the requested tags without exceeding allocated quota. Account has "
                                        + "a limit of " + this.account.getServiceLevel().getMaxTags() + " total tags.");
                    }
                }
            } catch (final SQLException sqlException) {
                throw ErrorTransformer.get("Failed to add tag batch", sqlException);
            }
        }

        @Override
        public void close() {
            try {
                if (this.batchCount > 0) {
                    this.added += IntStream.of(this.ps.executeBatch()).sum();
                    if (this.available - this.added < 0) {
                        throw new QuotaExceededException(
                                "Unable to add the requested tags without exceeding allocated quota. Account has "
                                        + "a limit of " + this.account.getServiceLevel().getMaxTags() + " total tags.");
                    }
                }
            } catch (final SQLException sqlException) {
                throw ErrorTransformer.get("Failed to execute tag insert batch", sqlException);
            } finally {
                try {
                    this.ps.close();
                } catch (final SQLException sqlException) {
                    throw ErrorTransformer.get("Failed to close prepared statement", sqlException);
                }
            }
        }
    }
}
