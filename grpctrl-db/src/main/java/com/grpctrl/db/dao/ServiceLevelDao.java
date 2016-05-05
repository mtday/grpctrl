package com.grpctrl.db.dao;

import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.common.util.CloseableBiConsumer;

import java.sql.Connection;

import javax.annotation.Nonnull;

/**
 * Defines the interface of the data access layer used to manage service level objects in the database.
 */
public interface ServiceLevelDao {
    /**
     * Create a consumer capable of adding service level objects to the database.
     *
     * @param conn the {@link Connection} to use when adding the service levels
     */
    CloseableBiConsumer<Long, ServiceLevel> getAddConsumer(@Nonnull Connection conn);
}
