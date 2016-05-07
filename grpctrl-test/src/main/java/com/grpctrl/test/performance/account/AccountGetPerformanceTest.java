package com.grpctrl.test.performance.account;

import com.grpctrl.client.AccountClient;
import com.grpctrl.common.model.EndPoint;
import com.grpctrl.test.performance.BasePerformanceTest;
import com.grpctrl.test.performance.PerformanceWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Run some performance tests on the account get-all capability.
 */
public class AccountGetPerformanceTest extends BasePerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(AccountGetPerformanceTest.class);

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
        new AccountGetPerformanceTest().runTests();
    }

    @Nonnull
    private final AccountClient client;

    public AccountGetPerformanceTest() {
        this.client = new AccountClient(getObjectMapper(), getHttpClient(), END_POINT);
    }

    public void runTests() throws InterruptedException {
        runTests(new AccountGetWorkerSupplier(this.client), TOTAL_REQUESTS, CONCURRENT);
    }

    private static class AccountGetWorkerSupplier implements Supplier<PerformanceWorker> {
        @Nonnull
        private final AccountClient client;
        @Nonnull
        private final List<Long> accountIds;

        @Nonnull
        private final Random random = new Random();

        /**
         * @param client the account client
         */
        public AccountGetWorkerSupplier(@Nonnull final AccountClient client) {
            this.client = Objects.requireNonNull(client);
            this.accountIds = new ArrayList<>();
            this.client.getAll(account -> accountIds.add(account.getId().orElse(null)));
        }

        @Override
        public PerformanceWorker get() {
            final long randomId = this.accountIds.get(random.nextInt(this.accountIds.size()));
            return new AccountGetWorker(this.client, randomId);
        }
    }

    private static class AccountGetWorker implements PerformanceWorker {
        @Nonnull
        private final AccountClient client;
        @Nonnull
        private final Long accountId;

        private long start = 0L;
        private long stop = 0L;

        @Nullable
        private Throwable failure = null;

        public AccountGetWorker(@Nonnull final AccountClient client, @Nonnull final Long accountId) {
            this.client = Objects.requireNonNull(client);
            this.accountId = Objects.requireNonNull(accountId);
        }

        @Override
        public void run() {
            this.start = System.currentTimeMillis();

            final AtomicInteger total = new AtomicInteger(0);
            try {
                this.client.get(this.accountId, account -> total.incrementAndGet());
            } catch (final Throwable failure) {
                this.failure = failure;
                LOG.error("Failed to get account by id " + this.accountId, failure);
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
