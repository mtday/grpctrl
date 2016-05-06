package com.grpctrl.test.performance;

import java.util.Optional;

/**
 * An interface for performance testing threads where the thread tracks how long it takes to run the task, and whether
 * an error occurred during the task processing.
 */
public interface PerformanceWorker extends Runnable {
    /**
     * Calculate and return the duration of the {@link Runnable#run} method, after the runnable has completed execution.
     *
     * @return the duration of the task processing, in milliseconds
     */
    long getDuration();

    /**
     * Retrieves the failure that occurred during the performance worker task processing, if available.
     *
     * @return the performance worker failure, possibly empty if no failure occurred
     */
    Optional<Throwable> getFailure();
}
