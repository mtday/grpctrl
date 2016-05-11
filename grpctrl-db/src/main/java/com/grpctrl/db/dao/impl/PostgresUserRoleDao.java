package com.grpctrl.db.dao.impl;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.db.dao.UserRoleDao;

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
 * Provides an implementation of a {@link UserRoleDao}.
 */
public class PostgresUserRoleDao implements UserRoleDao {
    @Override
    public Map<Long, Collection<UserRole>> get(
            @Nonnull final Connection conn, @Nonnull final Collection<Long> userIds) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(userIds);

        final String sql = "SELECT user_id, role FROM user_roles WHERE user_id = ANY (?)";

        final Map<Long, Collection<UserRole>> map = new HashMap<>();
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("bigint", userIds.toArray()));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final long userId = rs.getLong("user_id");
                    final UserRole userRole = UserRole.valueOf(rs.getString("role"));

                    Collection<UserRole> coll = map.get(userId);
                    if (coll == null) {
                        coll = new LinkedList<>();
                        map.put(userId, coll);
                    }

                    coll.add(userRole);
                }
            }
        } catch (final SQLException sqlException) {
            throw new InternalServerErrorException("Failed to retrieve user role data", sqlException);
        }
        return map;
    }

    @Override
    public void add(@Nonnull final Connection conn, @Nonnull final Collection<User> users) {
        Objects.requireNonNull(conn);
        Objects.requireNonNull(users);

        final int batchSize = 1000;
        final String sql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";

        int batches = 0;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            for (final User user : users) {
                ps.setLong(1, user.getId().orElse(null));
                for (final UserRole role : user.getRoles()) {
                    ps.setString(2, role.getName());
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
            throw new InternalServerErrorException("Failed to add user role data", sqlException);
        }
    }
}
