package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.dao.supplier.TagDaoSupplier;
import com.grpctrl.db.error.DaoException;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link GroupDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database.
 */
public class PostgresGroupDao implements GroupDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final TagDaoSupplier tagDaoSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     * @param tagDaoSupplier the {@link TagDaoSupplier} used to perform tag operations
     */
    public PostgresGroupDao(@Nonnull final DataSourceSupplier dataSourceSupplier, @Nonnull final TagDaoSupplier tagDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.tagDaoSupplier = Objects.requireNonNull(tagDaoSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private TagDaoSupplier getTagDaoSupplier() {
        return this.tagDaoSupplier;
    }

    @Override
    public int count(@Nonnull final Connection conn, @Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);

        final String sql = "SELECT COUNT(*) FROM groups WHERE account_id = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group count", sqlException);
        }
    }

    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final String groupName) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupName);

        final String sql = "SELECT COUNT(*) FROM groups WHERE account_id = ? AND group_name = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setString(2, groupName);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to check group existence by name", sqlException);
        }
    }

    private void consumeQuery(@Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final PreparedStatement ps) throws SQLException {
        try (final ResultSet rs = ps.executeQuery()) {
            final ResultSetIterator iter = new ResultSetIterator(rs);
            while (iter.hasNext()) {
                final Pair<Group, TagIterator> pair = iter.next();
                consumer.accept(pair.getLeft(), pair.getRight());
            }
            final Optional<SQLException> exception = iter.getException();
            if (exception.isPresent()) {
                throw exception.get();
            }
        }
    }

    @Override
    public void get(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (g.group_id = t.group_id) WHERE account_id = ? AND parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by id", sqlException);
        }
    }

    @Override
    public void get(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final Long groupId) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (g.group_id = t.group_id) WHERE account_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by id", sqlException);
        }
    }

    @Override
    public void get(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final String groupName) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupName);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (g.group_id = t.group_id) WHERE account_id = ? AND group_name = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setString(2, groupName);
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by name", sqlException);
        }
    }

    @Override
    public void children(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (g.group_id = t.group_id) WHERE account_id = ? AND parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve top-level groups", sqlException);
        }
    }

    @Override
    public void children(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final Long parentId) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);
        Objects.requireNonNull(parentId);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (g.group_id = t.group_id) WHERE account_id = ? AND parent_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, parentId);
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve children for group id", sqlException);
        }
    }

    @Override
    public void children(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final String parentName) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);
        Objects.requireNonNull(parentName);

        final String sql = "SELECT parent_id, group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t"
                + " ON (groups.group_id = tags.tag_id) WHERE account_id = ? AND parent_id IN "
                + "(SELECT group_id FROM groups where account_id = ? AND group_name = ?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, account.getId().orElse(null));
            ps.setString(3, parentName);
            consumeQuery(consumer, ps);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve children for parent group name", sqlException);
        }
    }

    @Override
    public void add(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final Iterable<Group> groups) throws DaoException {
        add(consumer, account, null, groups);
    }

    @Override
    public void add(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nullable final Long parentId, @Nonnull final Iterable<Group> groups) throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);
        Objects.requireNonNull(groups);

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection()) {
            add(consumer, conn, account, parentId, groups);
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }
    }

    private void add(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Connection conn,
            @Nonnull final Account account, @Nullable final Long parentId, @Nonnull final Iterable<Group> groups)
            throws DaoException {
        final int batchSize = 1000;
        final String sql = "INSERT INTO groups (account_id, parent_id, group_name) VALUES (?, ?, ?)";

        final TagDao tagDao = getTagDaoSupplier().get();
        try (final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             final CloseableBiConsumer<Long, Tag> tagAddConsumer = tagDao.getAddConsumer(conn, account)) {
            final Collection<Group> batch = new LinkedList<>();

            ps.setLong(1, account.getId().orElse(null));
            for (final Group group : groups) {
                if (parentId != null) {
                    ps.setLong(2, parentId);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, group.getName());
                ps.addBatch();
                batch.add(group.setParentId(parentId));

                if (batch.size() >= batchSize) {
                    consumeBatch(consumer, ps, batch, tagAddConsumer);
                }
            }
            if (!batch.isEmpty()) {
                consumeBatch(consumer, ps, batch, tagAddConsumer);
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }
    }

    private void consumeBatch(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final PreparedStatement ps,
            @Nonnull final Collection<Group> batch, final CloseableBiConsumer<Long, Tag> tagAddConsumer)
            throws DaoException, SQLException {
        ps.executeBatch();
        try (final ResultSet rs = ps.getGeneratedKeys()) {
            final Iterator<Group> iter = batch.iterator();
            while (rs.next() && iter.hasNext()) {
                final long groupId = rs.getLong(1);
                final Group group = iter.next();
                group.setId(groupId);

                for (final Tag tag : group.getTags()) {
                    tagAddConsumer.accept(groupId, tag);
                }
                consumer.accept(group, group.getTags().iterator());
            }
        }
        batch.clear();
    }

    @Override
    public int remove(
            @Nonnull final Account account, @Nonnull final Iterable<Long> groupIds) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupIds);

        int removed = 0;

        final int batchSize = 1000;
        final String sql = "DELETE FROM groups WHERE account_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            for (final Long groupId : groupIds) {
                ps.setLong(2, groupId);
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
            throw new DaoException("Failed to remove groups by id", sqlException);
        }

        return removed;
    }

    /**
     * This class is responsible for providing an iterator over {@link Tag} objects for streaming processing.
     */
    private static class TagIterator implements Iterator<Tag> {
        @Nonnull
        private final ResultSet rs;
        private final long groupId;

        private boolean hasNext = true;
        private Tag prev = new Tag();
        private Tag next = new Tag();

        private SQLException exception;

        /**
         * @param rs the result set from which tags will be read
         * @param groupId the unique id of the current group for which tags are being read
         */
        TagIterator(@Nonnull final ResultSet rs, final long groupId) {
            this.rs = rs;
            this.groupId = groupId;

            fetchNextReturnCurrent();
        }

        /**
         * @return whether more tags are available for this group
         */
        public boolean hasNext() {
            return this.hasNext;
        }

        /**
         * @return the next tag that has been pulled from the result set
         */
        @Nonnull
        public Tag next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            return fetchNextReturnCurrent();
        }

        @Nonnull
        private Tag fetchNextReturnCurrent() {
            try {
                if (this.hasNext && this.rs.next()) {
                    final long nextGroupId = this.rs.getLong("group_id");
                    if (this.groupId != nextGroupId) {
                        this.rs.previous();
                        this.hasNext = false;
                        return this.prev;
                    }

                    final String label = this.rs.getString("tag_label");
                    final String value = this.rs.getString("tag_value");
                    if (label == null || value == null) {
                        // No tag available.
                        return this.prev;
                    }

                    this.prev.setValues(this.next);
                    this.next.setLabel(label);
                    this.next.setValue(value);
                    this.hasNext = true;
                    return this.prev;
                }
            } catch (final SQLException sqlException) {
                this.exception = sqlException;
            }

            this.hasNext = false;
            return this.prev;
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

        private boolean hasNext = true;
        private MutablePair<Group, TagIterator> prev = new MutablePair<>(new Group(), null);
        private MutablePair<Group, TagIterator> next = new MutablePair<>(new Group(), null);

        private SQLException exception;

        /**
         * @param rs the result set from which the tags will be read
         */
        ResultSetIterator(@Nonnull final ResultSet rs) {
            this.rs = Objects.requireNonNull(rs);

            fetchNextReturnCurrent();
        }

        /**
         * @return whether more groups and tags exist
         */
        public boolean hasNext() {
            return this.hasNext;
        }

        /**
         * @return the next entry containing a group and an iterator of associated tags
         */
        @Nonnull
        public Pair<Group, TagIterator> next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            return fetchNextReturnCurrent();
        }

        @Nonnull
        private Pair<Group, TagIterator> fetchNextReturnCurrent() {
            try {
                if (this.hasNext && this.rs.next()) {
                    final long groupId = this.rs.getLong("group_id");
                    final long parentId = this.rs.getLong("parent_id");
                    final String groupName = this.rs.getString("group_name");
                    this.rs.previous(); // Back up one so we can read the tag for this current record.

                    this.prev.setLeft(this.next.getLeft());
                    this.prev.setRight(this.next.getRight());

                    this.next.getLeft().setId(groupId);
                    this.next.getLeft().setParentId(parentId);
                    this.next.getLeft().setName(groupName);
                    this.next.setRight(new TagIterator(this.rs, groupId));
                    this.hasNext = true;
                    return this.prev;
                }
            } catch (final SQLException sqlException) {
                this.exception = sqlException;
            }

            this.hasNext = false;
            return this.prev;
        }

        /**
         * @return any exception that occurred while iterating through the result set
         */
        @Nonnull
        public Optional<SQLException> getException() {
            if (this.exception != null) {
                return Optional.of(this.exception);
            }
            return this.prev.getValue().getException();
        }
    }
}
