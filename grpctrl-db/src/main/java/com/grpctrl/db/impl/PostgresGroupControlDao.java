package com.grpctrl.db.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.GroupControlDao;
import com.grpctrl.db.error.DaoException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link GroupControlDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database. Some PostgreSQL-specific SQL is used so this is not a generic DAO that can
 * be pointed at any JDBC database.
 */
@SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification = "just a "
        + "little code deduplication")
public class PostgresGroupControlDao implements GroupControlDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     */
    public PostgresGroupControlDao(@Nonnull final DataSourceSupplier dataSourceSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    @Override
    public Optional<Account> getAccount(@Nonnull final Long accountId) throws DaoException {
        Objects.requireNonNull(accountId);

        final String SQL =
                "SELECT a.name, s.max_groups, s.max_tags, s.max_depth FROM accounts a JOIN service_levels s ON (a"
                        + ".account_id = s.account_id) WHERE a.account_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, accountId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final String name = rs.getString("name");

                    final ServiceLevel.Builder serviceLevel = new ServiceLevel.Builder();
                    serviceLevel.setMaxGroups(rs.getInt("max_groups"));
                    serviceLevel.setMaxTags(rs.getInt("max_tags"));
                    serviceLevel.setMaxDepth(rs.getInt("max_depth"));

                    return Optional.of(new Account.Builder(name, serviceLevel.build()).setId(accountId).build());
                }

                return Optional.empty();
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve account", sqlException);
        }
    }

    @Nonnull
    @Override
    public Account addAccount(@Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(account);

        final String ACCOUNT_SQL = "INSERT INTO accounts (name) VALUES (?)";
        final String SERVICE_LEVEL_SQL =
                "INSERT INTO service_levels (account_id, max_groups, max_tags, max_depth) VALUES (?, ?, ?, ?)";

        final Account.Builder bld = new Account.Builder(account);

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement accountPs = conn.prepareStatement(ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS);
             final PreparedStatement svclevPs = conn.prepareStatement(SERVICE_LEVEL_SQL)) {
            accountPs.setString(1, account.getName());
            accountPs.executeUpdate();

            try (final ResultSet rs = accountPs.getGeneratedKeys()) {
                if (rs.next()) {
                    final long accountId = rs.getLong(1);
                    bld.setId(accountId);

                    svclevPs.setLong(1, accountId);
                    svclevPs.setInt(2, account.getServiceLevel().getMaxGroups());
                    svclevPs.setInt(3, account.getServiceLevel().getMaxTags());
                    svclevPs.setInt(4, account.getServiceLevel().getMaxDepth());
                    svclevPs.executeUpdate();
                }
            }

            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add account", sqlException);
        }

        return bld.build();
    }

    @Override
    public boolean removeAccount(@Nonnull final Long accountId) throws DaoException {
        Objects.requireNonNull(accountId);

        final String SQL = "DELETE FROM accounts WHERE account_id = ?";

        int removed = 0;

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, accountId);
            removed += ps.executeUpdate();
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove account", sqlException);
        }

        return removed > 0;
    }

    @Override
    public boolean groupExists(@Nonnull final Account account, @Nonnull final String groupName) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupName);

        final String SQL = "SELECT COUNT(*) FROM groups WHERE account_id = ? AND group_name = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    @Nonnull
    @Override
    public Optional<Group> getGroup(@Nonnull final Account account, @Nonnull final Long groupId) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String SQL = "SELECT parent_id, group_name FROM groups WHERE account_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final Group.Builder bld = new Group.Builder(rs.getString("group_name"));
                    bld.setId(groupId);

                    final long parentId = rs.getLong("parent_id");
                    if (!rs.wasNull()) {
                        bld.setParentId(parentId);
                    }

                    final Optional<Collection<Tag>> tags =
                            Optional.ofNullable(getTags(conn, account, Collections.singleton(groupId)).get(groupId));
                    if (tags.isPresent()) {
                        bld.addTags(tags.get());
                    }

                    return Optional.of(bld.build());
                }
            }

            return Optional.empty();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by id", sqlException);
        }
    }

    @Nonnull
    public Optional<Group> getRecursiveGroup(
            @Nonnull final Account account, @Nonnull final Long groupId) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String SQL = "" +
                "WITH RECURSIVE all_groups AS (" +
                "    SELECT parent_id, group_id, group_name, 1 AS depth" +
                "        FROM groups WHERE account_id = ? AND group_id = ?" +
                "    UNION ALL" +
                "    SELECT g.parent_id, g.group_id, group_name, depth + 1" +
                "        FROM groups g JOIN all_groups a ON" +
                "            (g.account_id = ? AND g.parent_id = a.group_id AND depth < ?)" +
                ")" +
                "SELECT parent_id, group_id, group_name, depth FROM all_groups LIMIT ?";

        final Map<Long, Group.Builder> map = new HashMap<>();

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, groupId);
            ps.setLong(3, account.getId().orElse(null));
            ps.setInt(4, account.getServiceLevel().getMaxDepth());
            ps.setInt(5, account.getServiceLevel().getMaxGroups());

            processRecursiveGroupsResultSet(ps, map, null);

            final Map<Long, Collection<Tag>> tags = getTags(conn, account, map.keySet());
            for (final Map.Entry<Long, Collection<Tag>> entry : tags.entrySet()) {
                map.get(entry.getKey()).addTags(entry.getValue());
            }

            return Optional.of(map.get(groupId).build());
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group recursively", sqlException);
        }
    }

    @Nonnull
    @Override
    public Collection<Group> getRecursiveGroups(@Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(account);

        final String SQL = "" +
                "WITH RECURSIVE all_groups AS (" +
                "    SELECT parent_id, group_id, group_name, 1 AS depth" +
                "        FROM groups WHERE account_id = ? AND parent_id IS NULL" +
                "    UNION ALL" +
                "    SELECT g.parent_id, g.group_id, group_name, depth + 1" +
                "        FROM groups g JOIN all_groups a ON" +
                "            (g.account_id = ? AND g.parent_id = a.group_id AND depth < ?)" +
                ")" +
                "SELECT parent_id, group_id, group_name, depth FROM all_groups LIMIT ?";

        final Map<Long, Group.Builder> map = new HashMap<>();
        final List<Group.Builder> topLevel = new LinkedList<>();

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, account.getId().orElse(null));
            ps.setInt(3, account.getServiceLevel().getMaxDepth());
            ps.setInt(4, account.getServiceLevel().getMaxGroups());

            processRecursiveGroupsResultSet(ps, map, topLevel);

            final Map<Long, Collection<Tag>> tags = getTags(conn, account, map.keySet());
            for (final Map.Entry<Long, Collection<Tag>> entry : tags.entrySet()) {
                map.get(entry.getKey()).addTags(entry.getValue());
            }

            return topLevel.stream().map(Group.Builder::build).collect(Collectors.toList());
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group recursively", sqlException);
        }
    }

    private void processRecursiveGroupsResultSet(
            @Nonnull final PreparedStatement ps, @Nonnull final Map<Long, Group.Builder> map,
            @Nullable final Collection<Group.Builder> topLevel) throws SQLException {
        try (final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final Group.Builder bld = new Group.Builder(rs.getString("group_name"));

                final long id = rs.getLong("group_id");
                bld.setId(id);

                final long parentId = rs.getLong("parent_id");
                if (!rs.wasNull()) {
                    bld.setParentId(parentId);

                    // The parent should always exist in the map already.
                    map.get(parentId).addChildBuilder(bld);
                } else if (topLevel != null) {
                    // No parent id, so this is a top-level group.
                    topLevel.add(bld);
                }

                map.put(id, bld);
            }
        }
    }

    @Override
    @Nonnull
    public Collection<Group> getGroupsByName(@Nonnull final Account account, @Nonnull final String groupName)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupName);

        final String SQL = "SELECT parent_id, group_id FROM groups WHERE account_id = ? AND group_name = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setString(2, groupName);

            final Map<Long, Group.Builder> matches = new HashMap<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Group.Builder bld = new Group.Builder(groupName);

                    final long groupId = rs.getLong("group_id");
                    bld.setId(groupId);

                    final Long parentId = rs.getLong("parent_id");
                    if (!rs.wasNull()) {
                        bld.setParentId(parentId);
                    }

                    matches.put(groupId, bld);
                }
            }

            final Map<Long, Collection<Tag>> tags = getTags(conn, account, matches.keySet());
            for (final Map.Entry<Long, Collection<Tag>> entry : tags.entrySet()) {
                matches.get(entry.getKey()).addTags(entry.getValue());
            }

            return matches.values().stream().map(Group.Builder::build).collect(Collectors.toList());
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by id", sqlException);
        }
    }

    @Nonnull
    @Override
    public Collection<Group> addGroups(
            @Nonnull final Account account, @Nonnull final Collection<Group> groups) throws DaoException {
        return addGroups(account, null, groups);
    }

    @Nonnull
    @Override
    public Collection<Group> addGroups(
            @Nonnull final Account account, @Nullable final Long parentId, @Nonnull final Collection<Group> groups)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groups);

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection()) {
            final Collection<Group> inserted = addGroups(conn, account, parentId, groups);
            conn.commit();
            return inserted;
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }
    }

    @Nonnull
    private Collection<Group> addGroups(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nullable final Long parentId,
            @Nonnull final Collection<Group> groups) throws DaoException {
        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return Collections.emptyList();
        }

        final Collection<Group> inserted = new ArrayList<>(groups.size());
        final List<Long> groupIds = new ArrayList<>(groups.size());

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO groups (account_id, parent_id, group_name) VALUES (?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            for (final Group group : groups) {
                if (parentId != null) {
                    ps.setLong(2, parentId);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, group.getName());
                ps.addBatch();
                batches++;

                if (batches > INSERT_BATCH_SIZE) {
                    ps.executeBatch();
                    batches = 0;

                    try (final ResultSet rs = ps.getGeneratedKeys()) {
                        while (rs.next()) {
                            groupIds.add(rs.getLong(1));
                        }
                    }
                }
            }
            if (batches > 0) {
                ps.executeBatch();

                try (final ResultSet rs = ps.getGeneratedKeys()) {
                    while (rs.next()) {
                        groupIds.add(rs.getLong(1));
                    }
                }
            }

            final Iterator<Long> idIter = groupIds.iterator();
            final Iterator<Group> groupIter = groups.iterator();

            while (idIter.hasNext() && groupIter.hasNext()) {
                final Long id = idIter.next();
                final Group original = groupIter.next();

                final Group.Builder bld = new Group.Builder(original.getName());
                bld.setId(id);
                bld.setParentId(parentId);

                bld.addChildren(addGroups(conn, account, id, original.getChildren()));
                bld.addTags(original.getTags());

                inserted.add(bld.build());
            }

            addTags(conn, account, inserted);
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }

        return inserted;
    }

    @Override
    public int removeGroups(
            @Nonnull final Account account, @Nonnull final Collection<Long> groupIds) throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupIds);

        if (groupIds.isEmpty()) {
            // Quick exit when no work to do.
            return 0;
        }

        int removed = 0;

        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM groups WHERE account_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setLong(1, account.getId().orElse(null));
            for (final Long groupId : groupIds) {
                ps.setLong(2, groupId);
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
            throw new DaoException("Failed to remove groups by id", sqlException);
        }

        return removed;
    }

    @Nonnull
    private Map<Long, Collection<Tag>> getTags(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Collection<Long> groupIds)
            throws DaoException {
        Map<Long, Collection<Tag>> map = new HashMap<>();

        if (groupIds.isEmpty()) {
            // Quick exit when no work to do.
            return map;
        }

        final String SQL =
                "SELECT group_id, tag_label, tag_value FROM tags WHERE account_id = ? AND group_id = ANY (?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setArray(2, conn.createArrayOf("varchar", groupIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Long groupId = rs.getLong("group_id");

                    Collection<Tag> collection = map.get(groupId);
                    if (collection == null) {
                        collection = new LinkedList<>();
                        map.put(groupId, collection);
                    }

                    if (collection.size() < account.getServiceLevel().getMaxTags()) {
                        collection.add(new Tag.Builder(rs.getString("tag_label"), rs.getString("tag_value")).build());
                    }
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve tags", sqlException);
        }

        return map;
    }

    private int addTags(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Collection<Group> groups)
            throws DaoException {
        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return 0;
        }

        int added = 0;

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

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
                    }
                }
            }
            if (batches > 0) {
                added += IntStream.of(ps.executeBatch()).sum();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags for groups", sqlException);
        }

        return added;
    }

    @Override
    public int addTags(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        try {
            return processTags(SQL, INSERT_BATCH_SIZE, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags", sqlException);
        }
    }

    @Override
    public int removeTags(
            @Nonnull final Account account, @Nonnull final Long groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ? AND tag_value = ?";

        try {
            return processTags(SQL, DELETE_BATCH_SIZE, account, groupId, tags);
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
    public int removeTagLabels(
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
