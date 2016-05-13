package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserEmail;
import com.grpctrl.db.dao.UserEmailDao;

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
 * Provides an implementation of a {@link UserEmailDao}.
 */
public class PostgresUserEmailDao implements UserEmailDao {
    @Override
    public Map<Long, Collection<UserEmail>> get(
            @Nonnull final Connection conn, @Nonnull final Collection<Long> userIds) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(userIds);

        final String sql = "SELECT user_id, email, is_primary, is_verified FROM user_emails WHERE user_id = ANY (?)";

        final Map<Long, Collection<UserEmail>> map = new HashMap<>();
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", userIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final long userId = rs.getLong("user_id");
                    final String email = rs.getString("email");
                    final boolean primary = rs.getBoolean("is_primary");
                    final boolean verified = rs.getBoolean("is_verified");

                    final UserEmail userEmail = new UserEmail(email, primary, verified);

                    Collection<UserEmail> coll = map.get(userId);
                    if (coll == null) {
                        coll = new LinkedList<>();
                        map.put(userId, coll);
                    }

                    coll.add(userEmail);
                }
            }
        } catch (final SQLException sqlException) {
            throw new InternalServerErrorException("Failed to retrieve user email data", sqlException);
        }
        return map;
    }

    @Override
    public void add(@Nonnull final Connection conn, @Nonnull final Collection<User> users) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(users);

        final int batchSize = 1000;
        final String sql = "INSERT INTO user_emails (user_id, email, is_primary, is_verified) VALUES (?, ?, ?, ?)";

        int batches = 0;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            for (final User user : users) {
                ps.setLong(1, user.getId().orElse(null));
                for (final UserEmail userEmail : user.getEmails()) {
                    ps.setString(2, userEmail.getEmail());
                    ps.setBoolean(3, userEmail.isPrimary());
                    ps.setBoolean(4, userEmail.isVerified());
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
            throw new InternalServerErrorException("Failed to add user email data", sqlException);
        }
    }
}
