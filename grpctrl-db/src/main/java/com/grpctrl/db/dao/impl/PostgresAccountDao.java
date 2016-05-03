package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Provides an implementation of an {@link AccountDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database.
 */
public class PostgresAccountDao implements AccountDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final ServiceLevelDaoSupplier serviceLevelDaoSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     * @param serviceLevelDaoSupplier the {@link ServiceLevelDaoSupplier} used to manage service level objects
     */
    public PostgresAccountDao(
            @Nonnull final DataSourceSupplier dataSourceSupplier,
            @Nonnull final ServiceLevelDaoSupplier serviceLevelDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.serviceLevelDaoSupplier = Objects.requireNonNull(serviceLevelDaoSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private ServiceLevelDaoSupplier getServiceLevelDaoSupplier() {
        return this.serviceLevelDaoSupplier;
    }

    @Override
    public void get(@Nonnull final Consumer<Account> consumer, @Nonnull final Long... accountIds) throws DaoException {
        get(consumer, Arrays.asList(Objects.requireNonNull(accountIds)));
    }

    @Override
    public void get(@Nonnull final Consumer<Account> consumer, @Nonnull final Collection<Long> accountIds)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(accountIds);

        final String sql = "SELECT a.account_id, a.name, s.max_groups, s.max_tags, s.max_depth FROM accounts a JOIN "
                + "service_levels s ON (a.account_id = s.account_id) WHERE a.account_id = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", accountIds.toArray()));

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final long accountId = rs.getLong("account_id");
                    final String name = rs.getString("name");

                    final ServiceLevel.Builder serviceLevel = new ServiceLevel.Builder();
                    serviceLevel.setMaxGroups(rs.getInt("max_groups"));
                    serviceLevel.setMaxTags(rs.getInt("max_tags"));
                    serviceLevel.setMaxDepth(rs.getInt("max_depth"));

                    consumer.accept(new Account.Builder(name, serviceLevel.build()).setId(accountId).build());
                }
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to get account", sqlException);
        }
    }

    @Override
    public void getAll(@Nonnull final Consumer<Account> consumer) throws DaoException {
        Objects.requireNonNull(consumer);

        final String sql = "SELECT a.account_id, a.name, s.max_groups, s.max_tags, s.max_depth FROM accounts a JOIN "
                + "service_levels s ON (a.account_id = s.account_id)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            final ServiceLevel.Builder serviceLevel = new ServiceLevel.Builder();

            while (rs.next()) {
                final long accountId = rs.getLong("account_id");
                final String name = rs.getString("name");

                serviceLevel.setMaxGroups(rs.getInt("max_groups"));
                serviceLevel.setMaxTags(rs.getInt("max_tags"));
                serviceLevel.setMaxDepth(rs.getInt("max_depth"));

                consumer.accept(new Account.Builder(name, serviceLevel.build()).setId(accountId).build());
            }
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to get all accounts", sqlException);
        }
    }

    @Override
    public void add(@Nonnull final Consumer<Account> consumer, @Nonnull final Account... accounts) throws DaoException {
        add(consumer, Arrays.asList(Objects.requireNonNull(accounts)));
    }

    @Override
    public void add(@Nonnull final Consumer<Account> consumer, @Nonnull final Collection<Account> accounts)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(accounts);

        final int batchSize = 1000;

        final String sql = "INSERT INTO accounts (name) VALUES (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            final Collection<Account> batch = new LinkedList<>();

            for (final Account account : accounts) {
                ps.setString(1, account.getName());
                ps.addBatch();
                batch.add(account);

                if (batch.size() >= batchSize) {
                    ps.executeBatch();
                    processBatch(conn, ps, batch, consumer);
                }
            }
            if (!batch.isEmpty()) {
                ps.executeBatch();
                processBatch(conn, ps, batch, consumer);
            }

            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add accounts", sqlException);
        }
    }

    private void processBatch(
            @Nonnull final Connection conn, @Nonnull final PreparedStatement ps,
            @Nonnull final Collection<Account> batch, @Nonnull final Consumer<Account> consumer)
            throws SQLException, DaoException {
        try (final ResultSet rs = ps.getGeneratedKeys()) {
            final Collection<Account> added = new ArrayList<>(batch.size());
            final Iterator<Account> batchIter = batch.iterator();
            while (rs.next() && batchIter.hasNext()) {
                final long accountId = rs.getLong(1);

                final Account account = new Account.Builder(batchIter.next()).setId(accountId).build();
                consumer.accept(account);
                added.add(account);
            }

            getServiceLevelDaoSupplier().get().add(conn, added);
            batch.clear();
        }
    }

    @Override
    public int remove(@Nonnull final Long... accountIds) throws DaoException {
        return remove(Arrays.asList(Objects.requireNonNull(accountIds)));
    }

    @Override
    public int remove(@Nonnull final Collection<Long> accountIds) throws DaoException {
        Objects.requireNonNull(accountIds);

        final String sql = "DELETE FROM accounts WHERE account_id = ANY (?)";

        int removed = 0;

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", accountIds.toArray()));
            removed += ps.executeUpdate();
            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to remove accounts", sqlException);
        }

        return removed;
    }
}
