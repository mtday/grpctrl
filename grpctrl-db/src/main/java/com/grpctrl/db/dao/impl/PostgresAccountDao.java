package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.ServiceLevelDao;
import com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier;
import com.grpctrl.db.error.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        final String sql =
                "SELECT a.account_id, a.name, s.max_groups, s.max_tags, s.max_depth FROM accounts a LEFT JOIN "
                        + "service_levels s ON (a.account_id = s.account_id) WHERE a.account_id = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", accountIds.toArray()));

            final Account account = new Account();

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    account.setId(rs.getLong("account_id"));
                    account.setName(rs.getString("name"));

                    account.getServiceLevel().setMaxGroups(rs.getInt("max_groups"));
                    account.getServiceLevel().setMaxTags(rs.getInt("max_tags"));
                    account.getServiceLevel().setMaxDepth(rs.getInt("max_depth"));

                    consumer.accept(account);
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

            final Account account = new Account();

            while (rs.next()) {
                account.setId(rs.getLong("account_id"));
                account.setName(rs.getString("name"));

                account.getServiceLevel().setMaxGroups(rs.getInt("max_groups"));
                account.getServiceLevel().setMaxTags(rs.getInt("max_tags"));
                account.getServiceLevel().setMaxDepth(rs.getInt("max_depth"));

                consumer.accept(account);
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
    public void add(@Nonnull final Consumer<Account> consumer, @Nonnull final Iterable<Account> accounts)
            throws DaoException {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(accounts);

        final int batchSize = 1000;

        final String sql = "INSERT INTO accounts (name) VALUES (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        final ServiceLevelDao serviceLevelDao = getServiceLevelDaoSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Need to close the service-level adder before committing.
            try (final CloseableBiConsumer<Long, ServiceLevel> serviceLevelAdder = serviceLevelDao
                    .getAddConsumer(conn)) {

                final Collection<Account> batch = new LinkedList<>();

                for (final Account account : accounts) {
                    ps.setString(1, account.getName());
                    ps.addBatch();
                    batch.add(account);

                    if (batch.size() >= batchSize) {
                        ps.executeBatch();
                        processBatch(ps, batch, serviceLevelAdder, consumer);
                    }
                }
                if (!batch.isEmpty()) {
                    ps.executeBatch();
                    processBatch(ps, batch, serviceLevelAdder, consumer);
                }
            }

            conn.commit();
        } catch (final SQLException sqlException) {
            throw new DaoException("Failed to add accounts", sqlException);
        }
    }

    private void processBatch(
            @Nonnull final PreparedStatement ps, @Nonnull final Collection<Account> batch,
            @Nonnull final CloseableBiConsumer<Long, ServiceLevel> serviceLevelAdder,
            @Nonnull final Consumer<Account> consumer) throws SQLException, DaoException {
        try (final ResultSet rs = ps.getGeneratedKeys()) {
            final Iterator<Account> batchIter = batch.iterator();
            while (rs.next() && batchIter.hasNext()) {
                final long accountId = rs.getLong(1);

                final Account account = batchIter.next();
                account.setId(accountId);

                consumer.accept(account);
                serviceLevelAdder.accept(accountId, account.getServiceLevel());
            }
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
