package com.grpctrl.test.performance.account;

import com.grpctrl.client.AccountClient;
import com.grpctrl.common.model.EndPoint;
import com.grpctrl.test.performance.BasePerformanceTest;
import com.grpctrl.test.performance.PerformanceWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Run some performance tests on the account get-all capability.
 */
public class AccountGetAllPerformanceTest extends BasePerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AccountGetAllPerformanceTest.class);

    private static final EndPoint END_POINT = new EndPoint();
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    private static final int TOTAL_REQUESTS = 1;
    private static final int CONCURRENT = 100;

    public static void main(final String... args) throws InterruptedException {
        new AccountGetAllPerformanceTest().runTests();
    }

    @Nonnull
    private final AccountClient client;

    public AccountGetAllPerformanceTest() {
        this.client = new AccountClient(getObjectMapper(), getHttpClient(), END_POINT, USERNAME, PASSWORD);
    }

    public void runTests() throws InterruptedException {
        runTests(new AccountGetAllWorkerSupplier(this.client), TOTAL_REQUESTS, CONCURRENT);
    }

    private static class AccountGetAllWorkerSupplier implements Supplier<PerformanceWorker> {
        @Nonnull
        private final AccountClient client;

        public AccountGetAllWorkerSupplier(@Nonnull final AccountClient client) {
            this.client = Objects.requireNonNull(client);
        }

        @Override
        public PerformanceWorker get() {
            return new AccountGetAllWorker(this.client);
        }
    }

    private static class AccountGetAllWorker implements PerformanceWorker {
        @Nonnull
        private final AccountClient client;

        private long start = 0L;
        private long stop = 0L;

        @Nullable
        private Throwable failure = null;

        public AccountGetAllWorker(@Nonnull final AccountClient client) {
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

        @Override
        public long getDuration() {
            return this.stop - this.start;
        }

        @Override
        public Optional<Throwable> getFailure() {
            return Optional.ofNullable(this.failure);
        }
    }
}
