package com.grpctrl.test.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.client.AccountClient;
import com.grpctrl.client.OkHttpClientSupplier;
import com.grpctrl.common.model.EndPoint;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.crypto.store.TrustStoreSupplier;
import com.grpctrl.test.LocalRunner;

import okhttp3.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Perform some performance testing on the account get-all capability.
 */
public class AccountGetAll extends LocalRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AccountGetAll.class);

    /**
     * The end-point for the back-end against which the performance tests will be executed.
     */
    private static final EndPoint END_POINT = new EndPoint.Builder().build();

    /**
     * The total number of get-all request operations to execute during the testing.
     */
    private static final int TOTAL_REQUESTS = 10000;

    /**
     * The number of concurrent threads to keep running when performing the operation under test.
     */
    private static final int THREADS = 100;

    /**
     * Run the performance tests.
     *
     * @param args ignored
     *
     * @throws Exception if there is a problem performing the tests
     */
    public static void main(final String... args) throws Exception {
        new AccountGetAll().runTests();
    }

    private final AccountClient client;
    private final ExecutorService testExecutor;
    private final ExecutorService clientExecutor;

    private AccountGetAll() {
        this.testExecutor = Executors.newFixedThreadPool(THREADS);
        this.clientExecutor = Executors.newFixedThreadPool(THREADS);

        final ConfigSupplier configSupplier = new ConfigSupplier(getConfig());

        final PasswordBasedEncryptionSupplier pbeSupplier = new PasswordBasedEncryptionSupplier(configSupplier);
        final KeyStoreSupplier keyStoreSupplier = new KeyStoreSupplier(configSupplier, pbeSupplier);
        final TrustStoreSupplier trustStoreSupplier = new TrustStoreSupplier(configSupplier, pbeSupplier);
        final SslContextSupplier sslContextSupplier =
                new SslContextSupplier(configSupplier, keyStoreSupplier, trustStoreSupplier, pbeSupplier);
        final OkHttpClient httpClient = new OkHttpClientSupplier(configSupplier, sslContextSupplier).get();

        final ObjectMapper objectMapper = new ObjectMapperSupplier().get();

        this.client = new AccountClient(objectMapper, httpClient, END_POINT);
    }

    private void runTests() throws Exception {
        final List<AccountGetAllThread> threads = new ArrayList<>(TOTAL_REQUESTS);

        final long start = System.currentTimeMillis();
        for (int threadNum = 0; threadNum < TOTAL_REQUESTS; threadNum++) {
            final AccountGetAllThread thread = new AccountGetAllThread(this.client);
            threads.add(thread);
            this.testExecutor.execute(thread);
        }

        this.testExecutor.shutdown();
        while (!this.testExecutor.isTerminated()) {
            this.testExecutor.awaitTermination(5, TimeUnit.MILLISECONDS);
        }
        final long stop = System.currentTimeMillis();

        this.clientExecutor.shutdown();

        logResults(threads, stop - start);
    }

    private void logResults(final List<AccountGetAllThread> threads, final long realTime) {
        final int errorCount = getErrorCount(threads);
        final List<Long> durations = getDurations(threads);
        final Long sum = getSum(durations);
        final Double mean = getMean(sum, durations.size());
        final Double median = getMedian(durations);
        final Long min = getMin(durations);
        final Long max = getMax(durations);

        LOG.info("Concurrent Threads: {}", THREADS);
        LOG.info("Total Requests:     {}", TOTAL_REQUESTS);
        LOG.info("Errors:             {}", errorCount);
        LOG.info("Duration Real Time: {}", DurationFormatUtils.formatDurationHMS(realTime));
        LOG.info("Duration Mean:      {}", DurationFormatUtils.formatDurationHMS(mean.longValue()));
        LOG.info("Duration Median:    {}", DurationFormatUtils.formatDurationHMS(median.longValue()));
        LOG.info("Duration Minimum:   {}", DurationFormatUtils.formatDurationHMS(min));
        LOG.info("Duration Maximum:   {}", DurationFormatUtils.formatDurationHMS(max));
        if (!durations.isEmpty()) {
            LOG.info("Duration First:     {}", DurationFormatUtils.formatDurationHMS(durations.get(0)));
        }

        if (threads.size() <= 30) {
            // Print the individual thread durations if there aren't that many threads.
            LOG.info("Individual durations for each thread:");
            int threadNum = 1;
            for (final AccountGetAllThread thread : threads) {
                LOG.info("Thread {}: {}", StringUtils.leftPad(String.valueOf(threadNum++), 2, '0'),
                        DurationFormatUtils.formatDurationHMS(thread.getDuration()));
            }
        }
    }

    private Double getMean(final Long sum, final int count) {
        if (count == 0) {
            return 0D;
        }

        return sum.doubleValue() / count;
    }

    private Double getMedian(final List<Long> durations) {
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

    private Long getMin(final List<Long> durations) {
        long min = Long.MAX_VALUE;
        for (final Long duration : durations) {
            min = Math.min(min, duration);
        }
        return min;
    }

    private Long getMax(final List<Long> durations) {
        long max = Long.MIN_VALUE;
        for (final Long duration : durations) {
            max = Math.max(max, duration);
        }
        return max;
    }

    private Long getSum(final List<Long> durations) {
        long sum = 0L;
        for (final Long duration : durations) {
            sum += duration;
        }
        return sum;
    }

    private int getErrorCount(final List<AccountGetAllThread> threads) {
        int count = 0;
        for (final AccountGetAllThread thread : threads) {
            if (thread.getFailure().isPresent()) {
                count++;
            }
        }
        return count;
    }

    private List<Long> getDurations(final List<AccountGetAllThread> threads) {
        return threads.stream().map(AccountGetAllThread::getDuration).collect(Collectors.toList());
    }

    private static class AccountGetAllThread implements Runnable {
        @Nonnull
        private final AccountClient client;

        private long start = 0L;
        private long stop = 0L;

        @Nullable
        private Throwable failure = null;

        public AccountGetAllThread(@Nonnull final AccountClient client) throws Exception {
            this.client = Objects.requireNonNull(client);
        }

        @Override
        public void run() {
            this.start = System.currentTimeMillis();

            final AtomicInteger total = new AtomicInteger(0);
            try {
                this.client.getAll(account -> total.incrementAndGet());
            } catch (final Throwable failure) {
                this.failure = failure;
                LOG.error("Failed to get all accounts", failure);
            } finally {
                this.stop = System.currentTimeMillis();
            }
        }

        public long getDuration() {
            return stop - start;
        }

        public Optional<Throwable> getFailure() {
            return Optional.ofNullable(this.failure);
        }
    }
}
