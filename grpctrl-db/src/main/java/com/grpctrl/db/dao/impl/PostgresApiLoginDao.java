package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.ApiLogin;
import com.grpctrl.common.util.CloseableBiConsumer;
import com.grpctrl.db.dao.ApiLoginDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;

/**
 * Provides an implementation of an {@link ApiLoginDao}.
 */
public class PostgresApiLoginDao implements ApiLoginDao {
    @Override
    public Map<Long, Collection<ApiLogin>> get(
            @Nonnull final Connection conn, @Nonnull final Collection<Long> accountIds) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(accountIds);

        final String sql = "SELECT account_id, key, secret FROM api_logins WHERE account_id = ANY (?)";

        final Map<Long, Collection<ApiLogin>> map = new HashMap<>();
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", accountIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final long accountId = rs.getLong("account_id");
                    final String key = rs.getString("key");
                    final String secret = rs.getString("secret");

                    final ApiLogin apiLogin = new ApiLogin(key, secret);

                    Collection<ApiLogin> coll = map.get(accountId);
                    if (coll == null) {
                        coll = new LinkedList<>();
                        map.put(accountId, coll);
                    }

                    coll.add(apiLogin);
                }
            }
        } catch (final SQLException sqlException) {
            throw new InternalServerErrorException("Failed to retrieve api login data", sqlException);
        }
        return map;
    }

    @Override
    public CloseableBiConsumer<Long, ApiLogin> getAddConsumer(@Nonnull final Connection conn) {
        return new AddConsumer(conn);
    }

    private static class AddConsumer implements CloseableBiConsumer<Long, ApiLogin> {
        private static final String SQL = "INSERT INTO api_logins (account_id, key, secret) VALUES (?, ?, ?)";

        private final PreparedStatement ps;

        public AddConsumer(@Nonnull final Connection conn) {
            try {
                this.ps = Objects.requireNonNull(conn).prepareStatement(SQL);
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException("Failed to create api login prepared statement", sqlException);
            }
        }

        @Override
        public void accept(@Nonnull final Long accountId, @Nonnull final ApiLogin apiLogin) {
            try {
                this.ps.setLong(1, accountId);
                this.ps.setString(2, apiLogin.getKey());
                this.ps.setString(3, apiLogin.getSecret());
                this.ps.addBatch();
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException("Failed to add api login batch", sqlException);
            }
        }

        @Override
        public void close() {
            try {
                this.ps.executeBatch();
            } catch (final SQLException sqlException) {
                throw new InternalServerErrorException("Failed to execute api login batch", sqlException);
            } finally {
                try {
                    this.ps.close();
                } catch (final SQLException sqlException) {
                    throw new InternalServerErrorException("Failed to close prepared statement", sqlException);
                }
            }
        }
    }
}
