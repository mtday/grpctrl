package com.grpctrl.rest.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * Responsible for logging memory usage statistics periodically.
 */
@Provider
public class MemoryUsageLogger implements ContainerRequestFilter, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsageLogger.class);

    @Inject
    public MemoryUsageLogger(@Nonnull final ScheduledExecutorService executorService) {
        Objects.requireNonNull(executorService).scheduleWithFixedDelay(this, 30, 120, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        final MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final double usedHeapMegs = heap.getUsed() / 1024d / 1024d;
        final double maxHeapMegs = heap.getMax() / 1024d / 1024d;
        final double heapPctUsed = usedHeapMegs / maxHeapMegs * 100d;

        final MemoryUsage nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        if (nonHeap.getMax() != -1) {
            final double usedNonHeapMegs = nonHeap.getUsed() / 1024d / 1024d;
            final double maxNonHeapMegs = nonHeap.getMax() / 1024d / 1024d;
            final double nonHeapPctUsed = usedNonHeapMegs / maxNonHeapMegs * 100d;

            LOG.info(String.format("Memory Usage: Heap %.0fM of %.0fM (%.2f%%), Non Heap %.0fM of %.0fM (%.2f%%)",
                    usedHeapMegs, maxHeapMegs, heapPctUsed, usedNonHeapMegs, maxNonHeapMegs, nonHeapPctUsed));
        } else {
            LOG.info(String.format("Memory Usage: Heap %.0fM of %.0fM (%.2f%%)", usedHeapMegs, maxHeapMegs,
                    heapPctUsed));
        }
    }

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext) throws IOException {
    }
}
