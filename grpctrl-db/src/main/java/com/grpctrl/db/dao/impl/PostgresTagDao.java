package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.error.DaoException;
import com.grpctrl.db.error.QuotaExceededException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link TagDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database.
 */
@SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification = "just a "
        + "little code deduplication, it really is constant")
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

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Override
    public int count(@Nonnull final Connection conn, @Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);

        final String SQL = "SELECT COUNT(*) FROM tags WHERE account_id = ?";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve tag count", sqlException);
        }
    }

    /**
     * This class is responsible for providing an iterator over {@link Tag} objects for streaming processing.
     */
    private static class TagIterator implements Iterator<Tag> {
        @Nullable
        private final ResultSet rs;
        private final long groupId;

        private Tag next;
        private SQLException exception;

        /**
         * @param rs the result set from which tags will be read
         * @param groupId the unique id of the current group for which tags are being read
         */
        TagIterator(@Nullable final ResultSet rs, final long groupId) {
            this.rs = rs;
            this.groupId = groupId;
        }

        /**
         * @return whether more tags are available for this group
         */
        public boolean hasNext() {
            if (this.next != null) {
                return true;
            } else {
                this.next = fetchNext();
            }
            return this.next != null;
        }

        /**
         * @return the next tag that has been pulled from the result set
         */
        @Nonnull
        public Tag next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            final Tag toReturn = this.next;
            this.next = fetchNext();
            return toReturn;
        }

        @Nullable
        private Tag fetchNext() {
            try {
                if (this.rs != null && this.rs.next()) {
                    final long nextGroupId = this.rs.getLong("group_id");
                    if (this.groupId != nextGroupId) {
                        this.rs.previous();
                        return null;
                    }
                    return new Tag.Builder(this.rs.getString("tag_label"), this.rs.getString("tag_value")).build();
                }
            } catch (final SQLException sqlException) {
                this.exception = sqlException;
            }
            return null;
        }

        /**
         * @return any exception that was thrown during the tag processing
         */
        @Nonnull
        public Optional<SQLException> getException() {
            return Optional.of(this.exception);
        }
    }

    /**
     * Responsible for reading tags from a result set for a given set of groups, chunking the tags up into iterators
     * so they can be processed sequentially.
     */
    private static class ResultSetIterator implements Iterator<Map.Entry<Group, TagIterator>> {
        @Nonnull
        private final ResultSet rs;
        @Nonnull
        private final Map<Long, Group> groupMap = new HashMap<>();

        private Map.Entry<Group, TagIterator> prev;
        private Map.Entry<Group, TagIterator> next;
        private SQLException exception;

        /**
         * @param rs the result set from which the tags will be read
         * @param groups the groups for which tags will be processed
         */
        ResultSetIterator(
                @Nonnull final ResultSet rs, @Nonnull final Collection<Group> groups) {
            this.rs = Objects.requireNonNull(rs);
            groups.stream().forEach(g -> this.groupMap.put(g.getId().orElse(null), g));
        }

        /**
         * @return whether more groups and tags exist
         */
        public boolean hasNext() {
            if (this.next != null) {
                return true;
            } else {
                this.next = fetchNext();
            }
            return this.next != null;
        }

        /**
         * @return the next entry containing a group and an iterator of associated tags
         */
        @Nonnull
        public Map.Entry<Group, TagIterator> next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.prev = this.next;
            this.next = null;
            return this.prev;
        }

        @Nullable
        private Map.Entry<Group, TagIterator> fetchNext() {
            try {
                if (this.rs.next()) {
                    final long groupId = this.rs.getLong("group_id");
                    this.rs.previous();

                    return new AbstractMap.SimpleEntry<>(this.groupMap.remove(groupId), new TagIterator(this.rs, groupId));
                }
            } catch (final SQLException sqlException) {
                this.exception = sqlException;
            }
            if (!this.groupMap.isEmpty()) {
                final Long groupId = this.groupMap.keySet().iterator().next();
                this.groupMap.remove(groupId);
                return new AbstractMap.SimpleEntry<>(this.groupMap.remove(groupId), new TagIterator(null, groupId));
            }
            return null;
        }

        /**
         * @return any exception that occurred while iterating through the result set
         */
        @Nonnull
        public Optional<SQLException> getException() {
            if (this.exception != null)
                return Optional.of(this.exception);
            return this.prev.getValue().getException();
        }
    }

    @Override
    public void get(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Connection conn,
            @Nonnull final Account account, @Nonnull final Collection<Group> groups) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);
        Objects.requireNonNull(groups);

        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final String SQL =
                "SELECT group_id, tag_label, tag_value FROM tags WHERE account_id = ? AND group_id = ANY (?) ORDER BY"
                        + " group_id LIMIT ?";

        final Collection<Long> groupIds =
                groups.stream().map(Group::getId).filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toList());

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("bigint", groupIds.toArray()));
            ps.setInt(3, account.getServiceLevel().getMaxTags());
            try (final ResultSet rs = ps.executeQuery()) {
                final ResultSetIterator iter = new ResultSetIterator(rs, groups);
                while (iter.hasNext()) {
                    final Map.Entry<Group, TagIterator> entry = iter.next();
                    consumer.accept(entry.getKey(), entry.getValue());

                    final Optional<SQLException> exception = iter.getException();
                    if (exception.isPresent()) {
                        throw exception.get();
                    }
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve tags", sqlException);
        }
    }

    @Override
    public int add(@Nonnull final BiConsumer<Group, Iterator<Tag>> consumer,
            @Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Collection<Group> groups)
            throws DaoException {
        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return 0;
        }

        int added = 0;

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        final int available = account.getServiceLevel().getMaxTags() - count(conn, account);

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            for (final Group group : groups) {
                ps.setLong(2, group.getId().orElse(null));

                for (final Tag tag : group.getTags()) {
                    ps.setString(3, tag.getLabel());
                    ps.setString(4, tag.getValue());
                    ps.addBatch();
                    batches++;

                    if (batches > INSERT_BATCH_SIZE) {
                        batches = 0;
                        added += IntStream.of(ps.executeBatch()).sum();
                        if (available - added < 0) {
                            throw new QuotaExceededException(
                                    "Unable to add the requested tags without exceeding allocated quota. Account has "
                                            + "a limit of " + account.getServiceLevel().getMaxTags() + " total tags.");
                        }
                    }
                }

                consumer.accept(group, group.getTags().iterator());
            }
            if (batches > 0) {
                added += IntStream.of(ps.executeBatch()).sum();
                if (available - added < 0) {
                    throw new QuotaExceededException(
                            "Unable to add the requested tags without exceeding allocated quota. Account has "
                                    + "a limit of " + account.getServiceLevel().getMaxTags() + " total tags.");
                }
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags for groups", sqlException);
        }

        return added;
    }

    @Override
    public int add(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        try {
            return processTags(SQL, 1000, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags", sqlException);
        }
    }

    @Override
    public int remove(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final String SQL = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ? AND tag_value = ?";

        try {
            return processTags(SQL, 1000, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove tags", sqlException);
        }
    }

    private int processTags(
            @Nonnull final String sql, final int batchSize, @Nonnull final Account account, @Nonnull final Long groupId,
            @Nonnull final Collection<Tag> tags) throws SQLException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tags);

        if (tags.isEmpty()) {
            // Quick exit when no work to do.
            return 0;
        }

        int modified = 0;

        final DataSource dataSource = getDataSourceSupplier().get();
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
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Collection<String> tagLabels)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tagLabels);

        if (tagLabels.isEmpty()) {
            // Quick exit when no work to do.
            return 0;
        }

        int removed = 0;
        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            for (final String tagLabel : tagLabels) {
                ps.setString(3, tagLabel);
                ps.addBatch();
                batches++;

                if (batches > DELETE_BATCH_SIZE) {
                    batches = 0;
                    removed += IntStream.of(ps.executeBatch()).sum();
                }
            }
            if (batches > 0) {
                removed += IntStream.of(ps.executeBatch()).sum();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove tags by label", sqlException);
        }

        return removed;
    }
}
