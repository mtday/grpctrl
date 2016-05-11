package com.grpctrl.db.dao;

import com.grpctrl.common.model.ApiLogin;
import com.grpctrl.common.util.CloseableBiConsumer;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage API login objects in the database.
 */
public interface ApiLoginDao {
    /**
     * Retrieve the {@link ApiLogin} objects for the specified accounts.
     *
     * @param conn the {@link Connection} to use when retrieving the API login objects
     * @param accountIds the unique identifiers of the accounts for which API login objects should be retrieved
     *
     * @return the requested {@link ApiLogin} objects, mapped by account id
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    Map<Long, Collection<ApiLogin>> get(@Nonnull Connection conn, @Nonnull Collection<Long> accountIds);

    /**
     * Create a consumer capable of adding API login objects to the database.
     *
     * @param conn the {@link Connection} to use when adding the API login objects
     *
     * @throws NullPointerException if either of the parameters are {@code null}
     */
    CloseableBiConsumer<Long, ApiLogin> getAddConsumer(@Nonnull Connection conn);
}
