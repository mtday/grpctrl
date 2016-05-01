package com.grpctrl.db.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.ParentId;
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
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link GroupControlDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end database.
 */
@SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", justification = "just a "
        + "little code deduplication")
public class DataSourceGroupControlDao implements GroupControlDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     */
    public DataSourceGroupControlDao(@Nonnull final DataSourceSupplier dataSourceSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Override
    public Optional<Account> getAccount(@Nonnull final String accountId) throws DaoException {
        Objects.requireNonNull(accountId);

        final String SQL =
                "SELECT s.max_groups, s.max_tags, s.max_children, s.max_depth FROM accounts a JOIN service_levels s "
                        + "ON (a.account_id = s.account_id) WHERE a.account_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, accountId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final ServiceLevel.Builder serviceLevel = new ServiceLevel.Builder();
                    serviceLevel.setMaxGroups(rs.getInt("max_groups"));
                    serviceLevel.setMaxTags(rs.getInt("max_tags"));
                    serviceLevel.setMaxChildren(rs.getInt("max_children"));
                    serviceLevel.setMaxDepth(rs.getInt("max_depth"));

                    return Optional.of(new Account.Builder(accountId, serviceLevel.build()).build());
                }

                return Optional.empty();
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve account", sqlException);
        }
    }

    @Override
    public void addAccount(@Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(account);

        final String ACCOUNT_SQL = "INSERT INTO accounts (account_id) VALUES (?)";
        final String SERVICE_LEVEL_SQL =
                "INSERT INTO service_levels (account_id, max_groups, max_tags, max_children, max_depth) VALUES (?, ?,"
                        + " ?, ?, ?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement accountPs = conn.prepareStatement(ACCOUNT_SQL);
             final PreparedStatement svclevPs = conn.prepareStatement(SERVICE_LEVEL_SQL)) {
            accountPs.setString(1, account.getId());
            accountPs.executeUpdate();

            svclevPs.setString(1, account.getId());
            svclevPs.setInt(2, account.getServiceLevel().getMaxGroups());
            svclevPs.setInt(3, account.getServiceLevel().getMaxTags());
            svclevPs.setInt(4, account.getServiceLevel().getMaxChildren());
            svclevPs.setInt(5, account.getServiceLevel().getMaxDepth());
            svclevPs.executeUpdate();

            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add account", sqlException);
        }
    }

    @Override
    public void removeAccount(@Nonnull final String accountId) throws DaoException {
        Objects.requireNonNull(accountId);

        final String SQL = "DELETE FROM accounts WHERE account_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, accountId);
            ps.executeUpdate();
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove account", sqlException);
        }
    }

    @Override
    public Optional<Group> getGroup(@Nonnull final Account account, @Nonnull final String groupId) throws DaoException {
        return getGroup(account, null, groupId);
    }

    @Override
    public Optional<Group> getGroup(
            @Nonnull final Account account, @Nullable final String parentId, @Nonnull final String groupId)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);

        final String SQL = "SELECT group_id FROM groups WHERE account_id = ? AND parent_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, account.getId());
            if (parentId != null) {
                ps.setString(2, parentId);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, groupId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Found the matching group, populate it and return.

                    final Set<String> groupIds = Collections.singleton(groupId);
                    final Map<String, Collection<Tag>> tags = getTags(conn, account, groupIds);
                    final Map<String, Collection<Group>> children =
                            getGroups(conn, account, groupIds, account.getServiceLevel().getMaxDepth() - 1,
                                    account.getServiceLevel().getMaxGroups() - groupIds.size());

                    final Collection<Tag> groupTags =
                            Optional.ofNullable(tags.get(groupId)).orElse(Collections.emptyList());
                    final Collection<Group> groupChildren =
                            Optional.ofNullable(children.get(groupId)).orElse(Collections.emptyList());

                    return Optional
                            .of(new Group.Builder(groupId).addTags(groupTags).addChildren(groupChildren).build());
                }
            }

            return Optional.empty();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve group by id", sqlException);
        }
    }

    @Override
    public Collection<Group> getGroups(@Nonnull final Account account) throws DaoException {
        return getTopLevelGroups(account);
    }

    private Collection<Group> getTopLevelGroups(@Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(account);

        List<Group> groups = new LinkedList<>();

        final String SQL = "SELECT group_id FROM groups WHERE account_id = ? AND parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, account.getId());

            final List<String> groupIds = new LinkedList<>();
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groupIds.add(rs.getString("group_id"));
                }
            }

            final Map<String, Collection<Tag>> tags = getTags(conn, account, groupIds);
            final Map<String, Collection<Group>> children =
                    getGroups(conn, account, groupIds, account.getServiceLevel().getMaxDepth() - 1,
                            account.getServiceLevel().getMaxGroups() - groupIds.size());

            for (final String groupId : groupIds) {
                final Collection<Tag> groupTags =
                        Optional.ofNullable(tags.get(groupId)).orElse(Collections.emptyList());
                final Collection<Group> groupChildren =
                        Optional.ofNullable(children.get(groupId)).orElse(Collections.emptyList());

                groups.add(new Group.Builder(groupId).addTags(groupTags).addChildren(groupChildren).build());
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve groups", sqlException);
        }

        return groups;
    }

    private Map<String, Collection<Group>> getGroups(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Collection<String> parentIds,
            final int depth, final int availableGroups) throws DaoException {
        final Map<String, Collection<Group>> map = new HashMap<>();

        if (parentIds.isEmpty() || depth == 0 || availableGroups <= 0) {
            // Quick exit if no parent ids were provided, or if we've already gone deep enough.
            return map;
        }

        final String SQL = "SELECT group_id, parent_id FROM groups WHERE account_id = ? AND parent_id = ANY (?)";

        // groupCounter is used to make sure we don't cross over the service level max groups configuration.
        int groupCounter = availableGroups;

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, account.getId());
            ps.setArray(2, conn.createArrayOf("varchar", parentIds.toArray()));

            final Collection<String> allGroupIds = new LinkedList<>();
            final Map<String, Collection<String>> pairs = new HashMap<>(); // parent_id -> [group_id]

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next() && groupCounter > 0) {
                    final String groupId = rs.getString("group_id");
                    final String parentId = rs.getString("parent_id");

                    Collection<String> groupIds = pairs.get(groupId);
                    if (groupIds == null) {
                        groupIds = new LinkedList<>();
                        pairs.put(parentId, groupIds);
                    }

                    groupIds.add(groupId);
                    allGroupIds.add(groupId);
                    groupCounter--;
                }
            }

            final Map<String, Collection<Tag>> tags = getTags(conn, account, allGroupIds);
            final Map<String, Collection<Group>> children =
                    getGroups(conn, account, allGroupIds, depth - 1, groupCounter);

            for (final Map.Entry<String, Collection<String>> entry : pairs.entrySet()) {
                final String parentId = entry.getKey();
                for (final String groupId : entry.getValue()) {
                    final Collection<Tag> groupTags =
                            Optional.ofNullable(tags.get(groupId)).orElse(Collections.emptyList());
                    final Collection<Group> groupChildren =
                            Optional.ofNullable(children.get(groupId)).orElse(Collections.emptyList());

                    final Group group =
                            new Group.Builder(groupId).addTags(groupTags).addChildren(groupChildren).build();

                    Collection<Group> groups = map.get(parentId);
                    if (groups == null) {
                        groups = new LinkedList<>();
                        map.put(parentId, groups);
                    }

                    if (groups.size() < account.getServiceLevel().getMaxChildren()) {
                        groups.add(group);
                    }
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve groups", sqlException);
        }

        return map;
    }

    @Override
    public void addGroups(@Nonnull final Account account, @Nonnull final Collection<Group> groups) throws DaoException {
        addGroups(account, null, groups);
    }

    @Override
    public void addGroups(
            @Nonnull final Account account, @Nullable final String parentId, @Nonnull final Collection<Group> groups)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groups);

        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO groups (account_id, parent_id, group_id) VALUES (?, ?, ?)";

        final Map<ParentId, Collection<Group>> flattened = Group.flatten(parentId, groups);

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setString(1, account.getId());
            for (final Map.Entry<ParentId, Collection<Group>> entry : flattened.entrySet()) {
                final Optional<String> optionalParentId = entry.getKey().getId();
                if (optionalParentId.isPresent()) {
                    ps.setString(2, optionalParentId.get());
                } else {
                    ps.setNull(2, Types.VARCHAR);
                }

                for (final Group group : entry.getValue()) {
                    ps.setString(3, group.getId());
                    ps.addBatch();
                    batches++;

                    if (batches > INSERT_BATCH_SIZE) {
                        ps.executeBatch();
                        batches = 0;
                    }
                }
            }
            if (batches > 0) {
                ps.executeBatch();
            }

            addTags(conn, account, flattened);
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }
    }

    @Override
    public void removeGroups(@Nonnull final Account account, @Nonnull final Collection<String> groupIds)
            throws DaoException {
        removeGroups(account, null, groupIds);
    }

    @Override
    public void removeGroups(
            @Nonnull final Account account, @Nullable final String parentId, @Nonnull final Collection<String> groupIds)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupIds);

        if (groupIds.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM groups WHERE account_id = ? AND parent_id = ? AND group_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setString(1, account.getId());
            if (parentId != null) {
                ps.setString(2, parentId);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            for (final String groupId : groupIds) {
                ps.setString(3, groupId);
                ps.addBatch();
                batches++;

                if (batches > DELETE_BATCH_SIZE) {
                    batches = 0;
                    ps.executeBatch();
                }
            }
            if (batches > 0) {
                ps.executeBatch();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove groups by id", sqlException);
        }
    }

    private Map<String, Collection<Tag>> getTags(
            @Nonnull final Connection conn, @Nonnull final Account account, @Nonnull final Collection<String> groupIds)
            throws DaoException {
        Map<String, Collection<Tag>> map = new HashMap<>();

        if (groupIds.isEmpty()) {
            // Quick exit when no work to do.
            return map;
        }

        final String SQL =
                "SELECT group_id, tag_label, tag_value FROM tags WHERE account_id = ? AND group_id = ANY (?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, account.getId());
            ps.setArray(2, conn.createArrayOf("varchar", groupIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final String groupId = rs.getString("group_id");
                    final Tag tag = new Tag.Builder(rs.getString("tag_label"), rs.getString("tag_value")).build();

                    Collection<Tag> collection = map.get(groupId);
                    if (collection == null) {
                        collection = new LinkedList<>();
                        map.put(groupId, collection);
                    }

                    if (collection.size() < account.getServiceLevel().getMaxTags()) {
                        collection.add(tag);
                    }
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve tags", sqlException);
        }

        return map;
    }

    @Override
    public void addTags(
            @Nonnull final Account account, @Nonnull final String groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        try {
            processTags(SQL, INSERT_BATCH_SIZE, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags", sqlException);
        }
    }

    private void addTags(
            @Nonnull final Connection conn, @Nonnull final Account account,
            @Nonnull final Map<ParentId, Collection<Group>> groups) throws DaoException {
        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES (?, ?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setString(1, account.getId());
            for (final Map.Entry<ParentId, Collection<Group>> entry : groups.entrySet()) {
                for (final Group group : entry.getValue()) {
                    ps.setString(2, group.getId());

                    for (final Tag tag : group.getTags()) {
                        ps.setString(3, tag.getLabel());
                        ps.setString(4, tag.getValue());
                        ps.addBatch();
                        batches++;

                        if (batches > INSERT_BATCH_SIZE) {
                            batches = 0;
                            ps.executeBatch();
                        }
                    }
                }
            }
            if (batches > 0) {
                ps.executeBatch();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add tags for groups", sqlException);
        }
    }

    @Override
    public void removeTags(
            @Nonnull final Account account, @Nonnull final String groupId, @Nonnull final Collection<Tag> tags)
            throws DaoException {
        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ? AND tag_value = ?";

        try {
            processTags(SQL, DELETE_BATCH_SIZE, account, groupId, tags);
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove tags", sqlException);
        }
    }

    private void processTags(
            @Nonnull final String sql, final int batchSize, @Nonnull final Account account,
            @Nonnull final String groupId, @Nonnull final Collection<Tag> tags) throws SQLException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tags);

        if (tags.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            int batches = 0;
            ps.setString(1, account.getId());
            ps.setString(2, groupId);
            for (final Tag tag : tags) {
                ps.setString(3, tag.getLabel());
                ps.setString(4, tag.getValue());
                ps.addBatch();
                batches++;

                if (batches > batchSize) {
                    batches = 0;
                    ps.executeBatch();
                }
            }
            if (batches > 0) {
                ps.executeBatch();
            }
            conn.commit();
        }
    }

    @Override
    public void removeTagLabels(
            @Nonnull final Account account, @Nonnull final String groupId, @Nonnull final Collection<String> tagLabels)
            throws DaoException {
        Objects.requireNonNull(account);
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(tagLabels);

        if (tagLabels.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final int DELETE_BATCH_SIZE = 1000;
        final String SQL = "DELETE FROM tags WHERE account_id = ? AND group_id = ? AND tag_label = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            int batches = 0;
            ps.setString(1, account.getId());
            ps.setString(2, groupId);
            for (final String tagLabel : tagLabels) {
                ps.setString(3, tagLabel);
                ps.addBatch();
                batches++;

                if (batches > DELETE_BATCH_SIZE) {
                    batches = 0;
                    ps.executeBatch();
                }
            }
            if (batches > 0) {
                ps.executeBatch();
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove tags by label", sqlException);
        }
    }
}
