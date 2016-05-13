package com.grpctrl.test.performance.status;

import com.grpctrl.client.AccountStatusClient;
import com.grpctrl.common.model.ApiLogin;
import com.grpctrl.common.model.EndPoint;
import com.grpctrl.test.performance.BasePerformanceTest;
import com.grpctrl.test.performance.PerformanceWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Run some performance tests on the account status capability.
 */
public class AccountStatusPerformanceTest extends BasePerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AccountStatusPerformanceTest.class);

    private static final EndPoint END_POINT = new EndPoint();
    private static final int TOTAL_REQUESTS = 10000;
    private static final int CONCURRENT = 100;

    public static void main(final String... args) throws InterruptedException {
        new AccountStatusPerformanceTest().runTests();
    }

    @Nonnull
    private final AccountStatusClient client;

    public AccountStatusPerformanceTest() {
        this.client = new AccountStatusClient(getObjectMapper(), getHttpClient(), END_POINT);
    }

    public void runTests() throws InterruptedException {
        runTests(new AccountStatusWorkerSupplier(this.client), TOTAL_REQUESTS, CONCURRENT);
    }

    private static class AccountStatusWorkerSupplier implements Supplier<PerformanceWorker> {
        @Nonnull
        private final AccountStatusClient client;
        @Nonnull
        private final ApiLogin apiLogin;

        public AccountStatusWorkerSupplier(@Nonnull final AccountStatusClient client) {
            this.client = Objects.requireNonNull(client);
            this.apiLogin = new ApiLogin("aaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }

        @Override
        public PerformanceWorker get() {
            return new AccountStatusWorker(this.client, this.apiLogin);
        }
    }

    private static class AccountStatusWorker implements PerformanceWorker {
        @Nonnull
        private final AccountStatusClient client;
        @Nonnull
        private final ApiLogin apiLogin;

        private long start = 0L;
        private long stop = 0L;

        @Nullable
        private Throwable failure = null;

        public AccountStatusWorker(@Nonnull final AccountStatusClient client, @Nonnull final ApiLogin apiLogin) {
            this.client = Objects.requireNonNull(client);
            this.apiLogin = Objects.requireNonNull(apiLogin);
        }

        @Override
        public void run() {
            this.start = System.currentTimeMillis();

            try {
                this.client.get(this.apiLogin);
            } catch (final Throwable failure) {
                this.failure = failure;
                LOG.error("Failed to get account status", failure);
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
