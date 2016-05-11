package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserAuth;
import com.grpctrl.common.model.UserEmail;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.model.UserSource;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.UserAuthDao;
import com.grpctrl.db.dao.UserDao;
import com.grpctrl.db.dao.UserEmailDao;
import com.grpctrl.db.dao.UserRoleDao;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;
import com.grpctrl.db.dao.supplier.UserAuthDaoSupplier;
import com.grpctrl.db.dao.supplier.UserEmailDaoSupplier;
import com.grpctrl.db.dao.supplier.UserRoleDaoSupplier;
import com.grpctrl.db.error.ErrorTransformer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Provides an implementation of a {@link UserDao} using a JDBC {@link DataSourceSupplier} to communicate
 * with a back-end PostgreSQL database.
 */
public class PostgresUserDao implements UserDao {
    @Nonnull
    private final DataSourceSupplier dataSourceSupplier;
    @Nonnull
    private final UserAuthDaoSupplier userAuthDaoSupplier;
    @Nonnull
    private final UserEmailDaoSupplier userEmailDaoSupplier;
    @Nonnull
    private final UserRoleDaoSupplier userRoleDaoSupplier;
    @Nonnull
    private final AccountDaoSupplier accountDaoSupplier;

    /**
     * @param dataSourceSupplier the supplier of the JDBC {@link DataSource} to use when communicating with the
     *     back-end database
     * @param userAuthDaoSupplier the {@link UserAuthDaoSupplier} used to manage user authorization objects
     * @param userEmailDaoSupplier the {@link UserEmailDaoSupplier} used to manage user email objects
     * @param userRoleDaoSupplier the {@link UserRoleDaoSupplier} used to manage user role objects
     * @param accountDaoSupplier the {@link AccountDaoSupplier} used to manage account objects
     */
    public PostgresUserDao(
            @Nonnull final DataSourceSupplier dataSourceSupplier,
            @Nonnull final UserAuthDaoSupplier userAuthDaoSupplier,
            @Nonnull final UserEmailDaoSupplier userEmailDaoSupplier,
            @Nonnull final UserRoleDaoSupplier userRoleDaoSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.userAuthDaoSupplier = Objects.requireNonNull(userAuthDaoSupplier);
        this.userEmailDaoSupplier = Objects.requireNonNull(userEmailDaoSupplier);
        this.userRoleDaoSupplier = Objects.requireNonNull(userRoleDaoSupplier);
        this.accountDaoSupplier = Objects.requireNonNull(accountDaoSupplier);
    }

    @Nonnull
    private DataSourceSupplier getDataSourceSupplier() {
        return this.dataSourceSupplier;
    }

    @Nonnull
    private UserAuthDaoSupplier getUserAuthDaoSupplier() {
        return this.userAuthDaoSupplier;
    }

    @Nonnull
    private UserEmailDaoSupplier getUserEmailDaoSupplier() {
        return this.userEmailDaoSupplier;
    }

    @Nonnull
    private UserRoleDaoSupplier getUserRoleDaoSupplier() {
        return this.userRoleDaoSupplier;
    }

    @Nonnull
    private AccountDaoSupplier getAccountDaoSupplier() {
        return this.accountDaoSupplier;
    }

    @Override
    public Optional<User> get(@Nonnull final Long userId) {
        final Collection<User> users = get(Collections.singleton(Objects.requireNonNull(userId)));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.iterator().next());
    }

    @Override
    public Collection<User> get(@Nonnull final Collection<Long> userIds) {
        Objects.requireNonNull(userIds);

        final String sql = "SELECT user_id, login, source FROM users WHERE user_id = ANY (?)";

        final Map<Long, User> map = new HashMap<>();
        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", userIds.toArray()));

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();

                    final long userId = rs.getLong("user_id");
                    user.setId(userId);
                    user.setLogin(rs.getString("login"));
                    user.setUserSource(UserSource.valueOf(rs.getString("source")));

                    map.put(userId, user);
                }

                enrich(conn, map);
            }
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to get users by id", sqlException);
        }
        return new TreeSet<>(map.values());
    }

    @Override
    public Optional<User> get(@Nonnull final UserSource source, @Nonnull final String login) {
        final Collection<User> users = get(source, Collections.singleton(Objects.requireNonNull(login)));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.iterator().next());
    }

    @Override
    public Collection<User> get(@Nonnull final UserSource source, @Nonnull final Collection<String> logins) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(logins);

        final String sql = "SELECT user_id, login FROM users WHERE source = ? AND login = ANY (?)";

        final Map<Long, User> map = new HashMap<>();
        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, source.name());
            ps.setArray(2, conn.createArrayOf("varchar", logins.toArray()));

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();

                    final long userId = rs.getLong("user_id");
                    user.setId(userId);
                    user.setLogin(rs.getString("login"));
                    user.setUserSource(source);

                    map.put(userId, user);
                }

                enrich(conn, map);
            }
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to get users by login", sqlException);
        }
        return new TreeSet<>(map.values());
    }

    private void enrich(@Nonnull final Connection conn, @Nonnull final Map<Long, User> users) {
        final UserAuthDao userAuthDao = getUserAuthDaoSupplier().get();
        final UserEmailDao userEmailDao = getUserEmailDaoSupplier().get();
        final UserRoleDao userRoleDao = getUserRoleDaoSupplier().get();
        final AccountDao accountDao = getAccountDaoSupplier().get();

        final Collection<Long> userIds = users.keySet();
        final Map<Long, UserAuth> userAuths = userAuthDao.get(conn, userIds);
        final Map<Long, Collection<UserEmail>> userEmails = userEmailDao.get(conn, userIds);
        final Map<Long, Collection<UserRole>> userRoles = userRoleDao.get(conn, userIds);
        final Map<Long, Collection<Account>> accounts = accountDao.getForUsers(conn, userIds);

        for (final Map.Entry<Long, UserAuth> entry : userAuths.entrySet()) {
            users.get(entry.getKey()).setUserAuth(entry.getValue());
        }
        for (final Map.Entry<Long, Collection<UserEmail>> entry : userEmails.entrySet()) {
            users.get(entry.getKey()).setEmails(entry.getValue());
        }
        for (final Map.Entry<Long, Collection<UserRole>> entry : userRoles.entrySet()) {
            users.get(entry.getKey()).setRoles(entry.getValue());
        }
        for (final Map.Entry<Long, Collection<Account>> entry : accounts.entrySet()) {
            users.get(entry.getKey()).setAccounts(entry.getValue());
        }
    }

    @Override
    public void add(@Nonnull final User user) {
        add(Collections.singleton(Objects.requireNonNull(user)));
    }

    @Override
    public void add(@Nonnull final Collection<User> users) {
        Objects.requireNonNull(users);

        final int batchSize = 1000;

        final String sql = "INSERT INTO users (login, source) VALUES (?, ?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            final Collection<User> batch = new LinkedList<>();

            for (final User user : users) {
                ps.setString(1, user.getLogin());
                ps.setString(2, user.getUserSource().name());
                ps.addBatch();
                batch.add(user);

                if (batch.size() >= batchSize) {
                    ps.executeBatch();
                    processBatch(conn, ps, batch);
                }
            }
            if (!batch.isEmpty()) {
                ps.executeBatch();
                processBatch(conn, ps, batch);
            }

            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to add users", sqlException);
        }
    }

    private void processBatch(
            @Nonnull final Connection conn, @Nonnull final PreparedStatement ps, @Nonnull final Collection<User> batch)
            throws SQLException {
        final UserAuthDao userAuthDao = getUserAuthDaoSupplier().get();
        final UserEmailDao userEmailDao = getUserEmailDaoSupplier().get();
        final UserRoleDao userRoleDao = getUserRoleDaoSupplier().get();

        try (final ResultSet rs = ps.getGeneratedKeys()) {
            final Iterator<User> batchIter = batch.iterator();
            while (rs.next() && batchIter.hasNext()) {
                final long userId = rs.getLong(1);

                final User user = batchIter.next();
                user.setId(userId);
            }

            userAuthDao.add(conn, batch);
            userEmailDao.add(conn, batch);
            userRoleDao.add(conn, batch);

            batch.clear();
        }
    }

    @Override
    public void remove(@Nonnull final Long userId) {
        remove(Collections.singleton(Objects.requireNonNull(userId)));
    }

    @Override
    public void remove(@Nonnull final Collection<Long> userIds) {
        Objects.requireNonNull(userIds);

        final String sql = "DELETE FROM users WHERE user_id = ANY (?)";

        final DataSource dataSource = getDataSourceSupplier().get();
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", userIds.toArray()));
            ps.executeUpdate();
            conn.commit();
        } catch (final SQLException sqlException) {
            throw ErrorTransformer.get("Failed to remove users", sqlException);
        }
    }
}
