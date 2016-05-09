package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.dao.supplier.TagDaoSupplier;
import com.grpctrl.db.error.ErrorTransformer;
import com.grpctrl.db.error.QuotaExceededException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
@SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
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
    public PostgresGroupDao(
            @Nonnull final DataSourceSupplier dataSourceSupplier, @Nonnull final TagDaoSupplier tagDaoSupplier) {
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
    public int count(@Nonnull final Connection conn, @Nonnull final Account account) {
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
            throw ErrorTransformer.get("Failed to retrieve group count", sqlException);
        }
    }

    @Override
    public int depth(@Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Long groupId) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String sql = "" +
                "WITH RECURSIVE parents AS (" +
                "    SELECT parent_id, group_id, group_name, 1 AS depth" +
                "        FROM groups WHERE account_id = ? AND group_id = ?" +
                "    UNION ALL" +
                "    SELECT g.parent_id, g.group_id, g.group_name, depth + 1" +
                "        FROM groups g JOIN parents p ON" +
                "            (g.account_id = ? AND g.group_id = p.parent_id)" +
                ")" +
                "SELECT MAX(depth) AS depth FROM parents;";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            ps.setLong(3, account.getId().orElse(null));
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve group count", sqlException);
        }
    }

    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final Long groupId) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String sql = "SELECT COUNT(*) FROM groups WHERE account_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to check group existence by id", sqlException);
        }
    }

    @Override
    public boolean exists(@Nonnull final Account account, @Nonnull final String groupName) {
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
            throw ErrorTransformer.get("Failed to check group existence by name", sqlException);
        }
    }

    private void consumeQuery(
            @Nonnull final PreparedStatement ps, @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer)
            throws SQLException {
        try (final ResultSet rs = ps.executeQuery()) {
            final Group group = new Group();
            final TagIterator tagIterator = new TagIterator(rs, group);
            int count = 0;
            while (tagIterator.hasMoreGroups()) {
                consumer.accept(group, tagIterator);

                final Optional<SQLException> exception = tagIterator.getException();
                if (exception.isPresent()) {
                    throw exception.get();
                }

                tagIterator.nextGroup();

                if (++count > 5) {
                    break;
                }
            }
        }
    }

    @Override
    public void get(@Nonnull final Account account, @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(consumer);

        final String sql =
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve group by id", sqlException);
        }
    }

    @Override
    public void getById(
            @Nonnull final Account account, @Nonnull final Collection<Long> groupIds,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupIds);
        Objects.requireNonNull(consumer);

        final String sql =
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "g.group_id = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("bigint", groupIds.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve group by id", sqlException);
        }
    }

    @Override
    public void getByName(
            @Nonnull final Account account, @Nonnull final Collection<String> groupNames,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupNames);
        Objects.requireNonNull(consumer);

        final String sql =
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "group_name = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("varchar", groupNames.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve groups by name", sqlException);
        }
    }

    @Override
    public void find(
            @Nonnull final Account account, @Nonnull final Collection<String> regexes, final boolean caseSensitive,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(regexes);
        Objects.requireNonNull(consumer);

        final String sql = String.format(
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "group_name %s ANY (?)", caseSensitive ? "~" : "~*");

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("varchar", regexes.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to find groups by regexes", sqlException);
        }
    }

    @Override
    public void childrenById(
            @Nonnull final Account account, @Nonnull final Collection<Long> parentIds,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(parentIds);
        Objects.requireNonNull(consumer);

        final String sql =
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "parent_id = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("bigint", parentIds.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve children for group id", sqlException);
        }
    }

    @Override
    public void childrenByName(
            @Nonnull final Account account, @Nonnull final Collection<String> parentNames,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(parentNames);
        Objects.requireNonNull(consumer);

        final String sql =
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "parent_id IN (SELECT group_id FROM groups where account_id = ? AND group_name = ANY (?))";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, account.getId().orElse(null));
            ps.setArray(3, conn.createArrayOf("varchar", parentNames.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve children for parent group name", sqlException);
        }
    }

    @Override
    public void childrenFind(
            @Nonnull final Account account, @Nonnull final Collection<String> regexes, final boolean caseSensitive,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(regexes);
        Objects.requireNonNull(consumer);

        final String sql = String.format(
                "SELECT parent_id, g.group_id, group_name, tag_label, tag_value FROM groups g LEFT JOIN tags t "
                        + "ON (g.group_id = t.group_id AND g.account_id = t.account_id) WHERE g.account_id = ? AND "
                        + "parent_id IN (SELECT group_id FROM groups where account_id = ? AND group_name %s ANY (?))",
                caseSensitive ? "~" : "~*");

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, account.getId().orElse(null));
            ps.setArray(3, conn.createArrayOf("varchar", regexes.toArray()));
            consumeQuery(ps, consumer);
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to retrieve children for parent group name", sqlException);
        }
    }

    @Override
    public void add(
            @Nonnull final Account account, @Nonnull final Iterator<Group> groups,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        add(account, null, groups, consumer);
    }

    @Override
    public void add(
            @Nonnull final Account account, @Nullable final Long parentId, @Nonnull final Iterator<Group> groups,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groups);
        Objects.requireNonNull(consumer);

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection()) {
            add(conn, account, parentId, groups, consumer);
            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to add groups", sqlException);
        }
    }

    private void add(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nullable final Long parentId,
            @Nonnull final Iterator<Group> groups, @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) {
        final int batchSize = 1000;
        final String sql = "INSERT INTO groups (account_id, parent_id, group_name) VALUES (?, ?, ?)";

        if (parentId != null) {
            final int currentDepth = depth(conn, account, parentId);
            if (currentDepth + 1 > account.getServiceLevel().getMaxDepth()) {
                throw new QuotaExceededException(
                        "Unable to add the requested groups without exceeding the account maximum "
                                + "group-within-group depth of " + account.getServiceLevel().getMaxDepth() + ".");
            }
        }

        final int available = account.getServiceLevel().getMaxGroups() - count(conn, account);
        int added = 0;

        final TagDao tagDao = getTagDaoSupplier().get();
        try (final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             final CloseableBiConsumer<Long, Tag> tagAddConsumer = tagDao.getAddConsumer(conn, account)) {
            final Collection<Group> batch = new LinkedList<>();

            ps.setLong(1, account.getId().orElse(null));
            while (groups.hasNext()) {
                final Group group = groups.next();
                if (parentId != null) {
                    ps.setLong(2, parentId);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, group.getName());
                ps.addBatch();
                batch.add(group.setParentId(parentId));

                if (batch.size() >= batchSize) {
                    added += consumeBatch(ps, batch, tagAddConsumer, consumer);
                    if (available - added < 0) {
                        throw new QuotaExceededException(
                                "Unable to add the requested groups without exceeding allocated quota. Account has "
                                        + "a limit of " + account.getServiceLevel().getMaxGroups() + " total groups.");
                    }
                }
            }
            if (!batch.isEmpty()) {
                added += consumeBatch(ps, batch, tagAddConsumer, consumer);
                if (available - added < 0) {
                    throw new QuotaExceededException(
                            "Unable to add the requested groups without exceeding allocated quota. Account has "
                                    + "a limit of " + account.getServiceLevel().getMaxGroups() + " total groups.");
                }
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to add groups", sqlException);
        }
    }

    private int consumeBatch(
            @Nonnull final PreparedStatement ps, @Nonnull final Collection<Group> batch,
            final CloseableBiConsumer<Long, Tag> tagAddConsumer,
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer) throws SQLException {
        final int added = IntStream.of(ps.executeBatch()).sum();
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
        return added;
    }

    @Override
    public int remove(
            @Nonnull final Account account, @Nonnull final Collection<Long> groupIds) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupIds);

        final String sql = "DELETE FROM groups WHERE account_id = ? AND group_id = ANY (?)";

        int removed = 0;

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("bigint", groupIds.toArray()));
            removed += IntStream.of(ps.executeUpdate()).sum();
            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to remove groups by id", sqlException);
        }

        return removed;
    }

    /**
     * This class is responsible for providing an iterator over {@link Tag} objects for streaming processing.
     */
    private static class TagIterator implements Iterator<Tag> {
        @Nonnull
        private final ResultSet rs;
        private final Group group;

        private boolean moveForward = true;
        private boolean hasMoreGroups = true;
        private boolean hasNext = true;
        private Tag prev = new Tag();
        private Tag next = new Tag();

        private SQLException exception;

        /**
         * @param rs the result set from which tags will be read
         * @param group the group into which the group data will be read
         */
        TagIterator(@Nonnull final ResultSet rs, final Group group) {
            this.rs = rs;
            this.group = group;

            nextGroup();
        }

        /**
         * @return whether more groups are available in the result set
         */
        public boolean hasMoreGroups() {
            return this.hasMoreGroups;
        }

        /**
         * Reset this tag iterator for the next group.
         */
        public void nextGroup() {
            try {
                if (!this.moveForward || this.rs.next()) {
                    this.moveForward = false;

                    // Process the current row in the result set.
                    group.setId(this.rs.getLong("group_id"));
                    final long parentId = this.rs.getLong("parent_id");
                    group.setParentId(this.rs.wasNull() ? null : parentId);
                    group.setName(this.rs.getString("group_name"));

                    final String label = this.rs.getString("tag_label");
                    final String value = this.rs.getString("tag_value");
                    if (label == null || value == null) {
                        // No tag available.
                        this.hasNext = false;
                        this.moveForward = true;
                    } else {
                        this.next.setLabel(label);
                        this.next.setValue(value);
                        this.hasNext = true;
                    }
                } else {
                    this.hasMoreGroups = false;
                    this.hasNext = false;
                }
            } catch (final SQLException sqlException) {
                this.exception = sqlException;
            }
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
                    if (nextGroupId != this.group.getId().orElse(null)) {
                        this.hasMoreGroups = true;
                        this.hasNext = false;
                        return this.next;
                    }

                    final String label = this.rs.getString("tag_label");
                    final String value = this.rs.getString("tag_value");
                    if (label == null || value == null) {
                        // No tag available.
                        this.hasNext = false;
                        this.moveForward = true;
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

            this.hasMoreGroups = false;
            this.hasNext = false;
            return this.next;
        }

        /**
         * @return any exception that was thrown during the tag processing
         */
        @Nonnull
        public Optional<SQLException> getException() {
            return Optional.ofNullable(this.exception);
        }
    }
}
