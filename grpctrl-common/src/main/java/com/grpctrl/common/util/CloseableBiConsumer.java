package com.grpctrl.common.util;

import java.sql.SQLException;
import java.util.function.BiConsumer;

/**
 * Defines the combined interface of a BiConsumer that should be closed after all of the objects have been consumed.
 */
public interface CloseableBiConsumer<A, B> extends BiConsumer<A, B>, AutoCloseable {
    void close() throws SQLException;
}
