package com.grpctrl.db.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.db.dao.AccountDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

/**
 * Provides a base unit test class responsible for testing {@link AccountDao} implementations.
 */
public abstract class BaseAccountDaoTest {
    // A consumer that ignores the accounts passed into it.
    private static final Consumer<Account> IGNORED = (account) -> {
    };

    /**
     * @return the {@link AccountDao} implementation to be tested
     */
    public abstract AccountDao getAccountDao();

    /**
     * @return the {@link AccountDao} implementation to be tested
     */
    public abstract AccountDao getAccountDaoWithDataSourceException();

    @Test
    public void testAccountManagement() throws WebApplicationException {
        final AccountDao dao = getAccountDao();

        final Account account1 = new Account("account-management-test-1");
        final Account account2 = new Account("account-management-test-2");
        final Account account3 = new Account("account-management-test-3");
        final Account account4 = new Account("account-management-test-4", new ServiceLevel(11, 12, 13));

        final Collection<Account> addedCollection = new ArrayList<>(1);
        dao.add(asList(account1, account2, account3, account4), new AddTo(addedCollection));

        // The add call will set the new id in each of the provided accounts.
        assertTrue(account1.getId().isPresent());
        assertTrue(account2.getId().isPresent());
        assertTrue(account3.getId().isPresent());
        assertTrue(account4.getId().isPresent());

        // The add call will call the consumer for each account, which will add them to the collection.
        assertEquals(4, addedCollection.size());
        assertTrue(addedCollection.contains(account1));
        assertTrue(addedCollection.contains(account2));
        assertTrue(addedCollection.contains(account3));
        assertTrue(addedCollection.contains(account4));

        // Pull out the ids.
        final Long account1id = account1.getId().orElse(null);
        final Long account2id = account2.getId().orElse(null);
        final Long account3id = account3.getId().orElse(null);
        final Long account4id = account4.getId().orElse(null);

        // Retrieving a non-existent account does not call the consumer.
        final Collection<Account> notFound = new ArrayList<>(1);
        dao.get(singleton(1111L), new AddTo(notFound));
        assertTrue(notFound.isEmpty());

        // Looking for a specific account id returns the expected account when it exists.
        final Collection<Account> found = new ArrayList<>(1);
        dao.get(singleton(account1id), new AddTo(found));
        assertEquals(1, found.size());
        assertTrue(found.contains(account1));

        // Looking for multiple account ids returns the expected accounts when they exist.
        final Collection<Account> multiple = new ArrayList<>(1);
        dao.get(asList(account1id, account2id), new AddTo(multiple));
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains(account1));
        assertTrue(multiple.contains(account2));

        // Looking for mixture of missing and available account ids returns only the ones that are available.
        final Collection<Account> mixture = new ArrayList<>(1);
        dao.get(asList(account1id, 2222L), new AddTo(mixture));
        assertEquals(1, mixture.size());
        assertTrue(mixture.contains(account1));

        // Retrieving all accounts consumes all of them.
        final Collection<Account> all = new ArrayList<>(1);
        dao.getAll(new AddTo(all));
        assertEquals(4, all.size());
        assertTrue(all.contains(account1));
        assertTrue(all.contains(account2));
        assertTrue(all.contains(account3));
        assertTrue(all.contains(account4));

        // Removing an account that does not exist returns a count of 0.
        assertEquals(0, dao.remove(singleton(1111L)));
        // Removing a single id that exists returns a count of 1.
        assertEquals(1, dao.remove(singleton(account1id)));
        // We just removed this account, so now expecting a 0 count.
        assertEquals(0, dao.remove(singleton(account1id)));
        // Removing a mixture of existing and missing ids returns the correct count.
        assertEquals(3, dao.remove(asList(account2id, 2222L, 3333L, account3id, account4id)));
    }

    @Test(expected = InternalServerErrorException.class)
    public void testGetAccountException() throws WebApplicationException {
        getAccountDaoWithDataSourceException().get(singleton(1111L), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testGetAllAccountException() throws WebApplicationException {
        getAccountDaoWithDataSourceException().getAll(IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testAddAccountException() throws WebApplicationException {
        getAccountDaoWithDataSourceException().add(singleton(new Account("add-account-exception")), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testRemoveAccountException() throws WebApplicationException {
        getAccountDaoWithDataSourceException().remove(singleton(1111L));
    }

    // A consumer that adds accounts to a collection.
    private static class AddTo implements Consumer<Account> {
        @Nonnull
        private final Collection<Account> collection;

        public AddTo(@Nonnull final Collection<Account> collection) {
            this.collection = Objects.requireNonNull(collection);
        }

        @Override
        public void accept(@Nonnull final Account account) {
            this.collection.add(new Account(account));
        }
    }
}
