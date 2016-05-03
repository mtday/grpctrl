package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.dao.TagDao;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
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
    private final TagDao tagDao;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     * @param tagDao the {@link TagDao} used to perform tag operations
     */
    public PostgresGroupDao(@Nonnull final DataSourceSupplier dataSourceSupplier, @Nonnull final TagDao tagDao) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.tagDao = Objects.requireNonNull(tagDao);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private TagDao getTagDao() {
        return this.tagDao;
    }

    @Override
    public int count(@Nonnull final Connection conn, @Nonnull final Account account) throws DaoException {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(account);

        final String SQL = "SELECT COUNT(*) FROM groups WHERE account_id = ?";

        try (final PreparedStatement ps = conn.prepareStatement(SQL)) {
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

    @Override
    public void get(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(account);

        final int BATCH_SIZE = 1000;
        final String SQL = "SELECT group_id, group_name FROM groups WHERE account_id = ? AND parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));

            final Collection<Group> batch = new LinkedList<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final long groupId = rs.getLong("group_id");
                    final Group.Builder bld = new Group.Builder(rs.getString("group_name")).setId(groupId);

                    final long parentId = rs.getLong("parent_id");
                    if (!rs.wasNull()) {
                        bld.setParentId(parentId);
                    }

                    batch.add(bld.build());
                    if (batch.size() >= BATCH_SIZE) {
                        getTagDao().get(consumer, conn, account, batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    getTagDao().get(consumer, conn, account, batch);
                }
            }
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

                    getTagDao().get(consumer, conn, account, Collections.singleton(bld.build()));
                }
            }
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

        final int batchSize = 1000;
        final String SQL = "SELECT parent_id, group_id FROM groups WHERE account_id = ? AND group_name = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setString(2, groupName);

            final Collection<Group> batch = new LinkedList<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Group.Builder bld = new Group.Builder(groupName).setId(rs.getLong("group_id"));

                    final Long parentId = rs.getLong("parent_id");
                    if (!rs.wasNull()) {
                        bld.setParentId(parentId);
                    }

                    batch.add(bld.build());

                    if (batch.size() >= batchSize) {
                        getTagDao().get(consumer, conn, account, batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    getTagDao().get(consumer, conn, account, batch);
                }
            }
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

        final int batchSize = 1000;
        final String SQL = "SELECT group_id, group_name FROM groups WHERE account_id = ? AND parent_id IS NULL";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));

            final Collection<Group> batch = new LinkedList<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batch.add(new Group.Builder(rs.getString("group_name")).setId(rs.getLong("group_id")).build());

                    if (batch.size() >= batchSize) {
                        getTagDao().get(consumer, conn, account, batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    getTagDao().get(consumer, conn, account, batch);
                }
            }
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

        final int batchSize = 1000;
        final String SQL = "SELECT group_id, group_name FROM groups WHERE account_id = ? AND parent_id = ?";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, parentId);

            final Collection<Group> batch = new LinkedList<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batch.add(new Group.Builder(rs.getString("group_name")).setId(rs.getLong("group_id"))
                            .setParentId(parentId).build());

                    if (batch.size() >= batchSize) {
                        getTagDao().get(consumer, conn, account, batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    getTagDao().get(consumer, conn, account, batch);
                }
            }
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

        final int batchSize = 1000;
        final String SQL = "SELECT parent_id, group_id, group_name FROM groups WHERE account_id = ? AND parent_id IN "
                + "(SELECT group_id FROM groups where account_id = ? AND group_name = ?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setLong(1, account.getId().orElse(null));
            ps.setLong(2, account.getId().orElse(null));
            ps.setString(3, parentName);

            final Collection<Group> batch = new LinkedList<>();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    batch.add(new Group.Builder(rs.getString("group_name")).setId(rs.getLong("group_id"))
                            .setParentId(rs.getLong("parent_id")).build());

                    if (batch.size() >= batchSize) {
                        getTagDao().get(consumer, conn, account, batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    getTagDao().get(consumer, conn, account, batch);
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to retrieve children for parent group name", sqlException);
        }
    }

    @Override
    public void add(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nonnull final Collection<Group> groups) throws DaoException {
        add(consumer, account, null, groups);
    }

    @Override
    public void add(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Account account,
            @Nullable final Long parentId, @Nonnull final Collection<Group> groups) throws DaoException {
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
            @Nonnull final Account account, @Nullable final Long parentId, @Nonnull final Collection<Group> groups)
            throws DaoException {
        if (groups.isEmpty()) {
            // Quick exit when no work to do.
            return;
        }

        final int INSERT_BATCH_SIZE = 1000;
        final String SQL = "INSERT INTO groups (account_id, parent_id, group_name) VALUES (?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            final Collection<Group.Builder> batch = new LinkedList<>();

            ps.setLong(1, account.getId().orElse(null));
            for (final Group group : groups) {
                if (parentId != null) {
                    ps.setLong(2, parentId);
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, group.getName());
                ps.addBatch();
                batch.add(new Group.Builder(group).setParentId(parentId));

                if (batch.size() >= INSERT_BATCH_SIZE) {
                    consumeBatch(consumer, conn, ps, account, batch);
                }
            }
            if (!batch.isEmpty()) {
                consumeBatch(consumer, conn, ps, account, batch);
            }
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add groups", sqlException);
        }
    }

    private void consumeBatch(
            @Nonnull final BiConsumer<Group, Iterator<Tag>> consumer, @Nonnull final Connection conn,
            @Nonnull final PreparedStatement ps, @Nonnull final Account account,
            @Nonnull final Collection<Group.Builder> batch) throws DaoException, SQLException {
        ps.executeBatch();
        try (final ResultSet rs = ps.getGeneratedKeys()) {
            final Iterator<Group.Builder> iter = batch.iterator();
            while (rs.next() && iter.hasNext()) {
                iter.next().setId(rs.getLong(1));
            }
        }
        getTagDao().add(consumer, conn, account, batch.stream().map(Group.Builder::build).collect(Collectors.toList()));
        batch.clear();
    }

    @Override
    public int remove(
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
}
