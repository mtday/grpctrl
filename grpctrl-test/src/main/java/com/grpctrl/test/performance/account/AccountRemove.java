package com.grpctrl.test.performance.account;

import com.grpctrl.client.AccountClient;
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
 * Run some performance tests on the account remove capability.
 */
public class AccountRemove extends BasePerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRemove.class);

    private static final EndPoint END_POINT = new EndPoint();
    private static final int TOTAL_REQUESTS = 10000;
    private static final int CONCURRENT = 100;

    /**
     * Run the performance tests.
     *
     * @param args ignored
     *
     * @throws InterruptedException if there is a problem performing the tests
     */
    public static void main(final String... args) throws InterruptedException {
        new AccountRemove().runTests();
    }

    @Nonnull
    private final AccountClient client;

    public AccountRemove() {
        this.client = new AccountClient(getObjectMapper(), getHttpClient(), END_POINT);
    }

    public void runTests() throws InterruptedException {
        runTests(new AccountRemoveWorkerSupplier(this.client), TOTAL_REQUESTS, CONCURRENT);
    }

    private static class AccountRemoveWorkerSupplier implements Supplier<PerformanceWorker> {
        @Nonnull
        private final AccountClient client;

        /**
         * @param client the account client
         */
        public AccountRemoveWorkerSupplier(@Nonnull final AccountClient client) {
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

            try {
                this.client.remove(1L);
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
