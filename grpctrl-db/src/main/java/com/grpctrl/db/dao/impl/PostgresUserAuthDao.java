package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserAuth;
import com.grpctrl.db.dao.UserAuthDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;

/**
 * Provides an implementation of a {@link UserAuthDao}.
 */
public class PostgresUserAuthDao implements UserAuthDao {
    @Override
    public Map<Long, UserAuth> get(
            @Nonnull final Connection conn, @Nonnull final Collection<Long> userIds) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(userIds);

        final String sql = "SELECT user_id, hash_alg, salt, hashed_pass FROM user_auths WHERE user_id = ANY (?)";

        final Map<Long, UserAuth> map = new HashMap<>();
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", userIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final long userId = rs.getLong("user_id");
                    final String hashAlgorithm = rs.getString("hash_alg");
                    final String salt = rs.getString("salt");
                    final String hashedPass = rs.getString("hashed_pass");

                    map.put(userId, new UserAuth(hashAlgorithm, salt, hashedPass));
                }
            }
        } catch (final SQLException sqlException) {
            throw new InternalServerErrorException("Failed to retrieve user auth data", sqlException);
        }
        return map;
    }

    @Override
    public void add(@Nonnull final Connection conn, @Nonnull final Collection<User> users) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(users);

        final int batchSize = 1000;
        final String sql = "INSERT INTO user_auths (user_id, hash_alg, salt, hashed_pass) VALUES (?, ?, ?, ?)";

        int batches = 0;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            for (final User user : users) {
                final Optional<UserAuth> userAuth = user.getUserAuth();
                if (userAuth.isPresent()) {
                    ps.setLong(1, user.getId().orElse(null));
                    ps.setString(2, userAuth.get().getHashAlgorithm());
                    ps.setString(3, userAuth.get().getSalt());
                    ps.setString(4, userAuth.get().getHashedPass());
                    ps.addBatch();
                    batches++;

                    if (batches >= batchSize) {
                        batches = 0;
                        ps.executeBatch();
                    }
                }
            }

            if (batches > 0) {
                ps.executeBatch();
            }
        } catch (final SQLException sqlException) {
            throw new InternalServerErrorException("Failed to add user auth data", sqlException);
        }
    }
}
