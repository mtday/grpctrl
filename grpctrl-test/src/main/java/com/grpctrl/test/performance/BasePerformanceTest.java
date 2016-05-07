package com.grpctrl.test.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.client.OkHttpClientSupplier;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.test.LocalRunner;

import okhttp3.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Perform some performance testing on the account get-all capability.
 */
public class BasePerformanceTest extends LocalRunner {
    private static final Logger LOG = LoggerFactory.getLogger(BasePerformanceTest.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Default constructor initializes internal objects.
     */
    public BasePerformanceTest() {
        final ConfigSupplier configSupplier = new ConfigSupplier(getConfig());
        final PasswordBasedEncryptionSupplier pbeSupplier = new PasswordBasedEncryptionSupplier(configSupplier);
        final KeyStoreSupplier keyStoreSupplier = new KeyStoreSupplier(configSupplier, pbeSupplier);
        final SslContextSupplier sslContextSupplier =
                new SslContextSupplier(configSupplier, keyStoreSupplier, pbeSupplier);

        this.httpClient = new OkHttpClientSupplier(configSupplier, sslContextSupplier).get();
        this.objectMapper = new ObjectMapperSupplier().get();
    }

    /**
     * @return a configured {@link OkHttpClient} capable of communicating with the back-end service
     */
    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * @return a configured {@link ObjectMapper} for deserializing client JSON objects
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Run the performance tests.
     *
     * @param workerSupplier the supplier responsible for providing the worker threads
     * @param workerCount the total number of worker threads to create
     * @param concurrent the number of concurrent threads to keep in flight at one time
     *
     * @throws InterruptedException if the executor service is interrupted
     */
    public void runTests(
            @Nonnull final Supplier<PerformanceWorker> workerSupplier, final int workerCount, final int concurrent)
            throws InterruptedException {
        final ExecutorService testExecutor = Executors.newFixedThreadPool(concurrent);

        final List<PerformanceWorker> workers = new ArrayList<>(workerCount);

        final long start = System.currentTimeMillis();
        for (int workerNum = 0; workerNum < workerCount; workerNum++) {
            final PerformanceWorker worker = workerSupplier.get();
            workers.add(worker);
            testExecutor.execute(worker);
        }

        testExecutor.shutdown();
        while (!testExecutor.isTerminated()) {
            testExecutor.awaitTermination(5, TimeUnit.MILLISECONDS);
        }
        final long stop = System.currentTimeMillis();

        logResults(workers, concurrent, stop - start);
    }

    private void logResults(
            @Nonnull final List<PerformanceWorker> workers, final int concurrentThreads, final long realTime) {
        final int errorCount = getErrorCount(workers);
        final List<Long> durations = getDurations(workers);
        final Long sum = getSum(durations);
        final Double mean = getMean(sum, durations.size());
        final Double median = getMedian(durations);
        final Long min = getMin(durations);
        final Long max = getMax(durations);

        LOG.info("Concurrent Threads: {}", concurrentThreads);
        LOG.info("Total Requests:     {}", workers.size());
        LOG.info("Errors:             {}", errorCount);
        LOG.info("Duration Real Time: {}", DurationFormatUtils.formatDurationHMS(realTime));
        LOG.info("Duration Mean:      {}", DurationFormatUtils.formatDurationHMS(mean.longValue()));
        LOG.info("Duration Median:    {}", DurationFormatUtils.formatDurationHMS(median.longValue()));
        LOG.info("Duration Minimum:   {}", DurationFormatUtils.formatDurationHMS(min));
        LOG.info("Duration Maximum:   {}", DurationFormatUtils.formatDurationHMS(max));
        if (!durations.isEmpty()) {
            LOG.info("Duration First:     {}", DurationFormatUtils.formatDurationHMS(durations.get(0)));
        }

        if (workers.size() <= 30) {
            // Print the individual thread durations if there aren't that many threads.
            LOG.info("Individual durations for each thread:");
            int threadNum = 1;
            for (final PerformanceWorker worker : workers) {
                LOG.info("Worker {}: {}", StringUtils.leftPad(String.valueOf(threadNum++), 2, '0'),
                        DurationFormatUtils.formatDurationHMS(worker.getDuration()));
            }
        }
    }

    private double getMean(final long sum, final int count) {
        if (count == 0) {
            return 0D;
        }

        return ((double) sum) / count;
    }

    private double getMedian(@Nonnull final List<Long> durations) {
        if (durations.isEmpty()) {
            return 0D;
        }

        if (durations.size() % 2 == 1) {
            return durations.get(durations.size() / 2).doubleValue();
        } else {
            final Double first = durations.get(durations.size() / 2 - 1).doubleValue();
            final Double second = durations.get(durations.size() / 2).doubleValue();
            return (first + second) / 2;
        }
    }

    private Long getMin(@Nonnull final List<Long> durations) {
        long min = Long.MAX_VALUE;
        for (final Long duration : durations) {
            min = Math.min(min, duration);
        }
        return min;
    }

    private long getMax(@Nonnull final List<Long> durations) {
        long max = Long.MIN_VALUE;
        for (final Long duration : durations) {
            max = Math.max(max, duration);
        }
        return max;
    }

    private long getSum(@Nonnull final List<Long> durations) {
        long sum = 0L;
        for (final Long duration : durations) {
            sum += duration;
        }
        return sum;
    }

    private int getErrorCount(@Nonnull final List<? extends PerformanceWorker> workers) {
        int count = 0;
        for (final PerformanceWorker worker : workers) {
            if (worker.getFailure().isPresent()) {
                count++;
            }
        }
        return count;
    }

    @Nonnull
    private List<Long> getDurations(@Nonnull final List<? extends PerformanceWorker> threads) {
        Objects.requireNonNull(threads);
        return threads.stream().map(PerformanceWorker::getDuration).collect(Collectors.toList());
    }
}
