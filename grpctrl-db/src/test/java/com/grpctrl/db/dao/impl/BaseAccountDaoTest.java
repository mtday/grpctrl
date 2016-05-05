package com.grpctrl.db.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.error.DaoException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

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
    public void testAccountManagement() throws DaoException {
        final AccountDao dao = getAccountDao();

        final Account account1 = new Account("account-management-test-1", new ServiceLevel());
        final Account account2 = new Account("account-management-test-2", new ServiceLevel());
        final Account account3 = new Account("account-management-test-3", new ServiceLevel());
        final Account account4 = new Account("account-management-test-4", new ServiceLevel());

        final Collection<Account> addedCollection = new ArrayList<>(1);
        dao.add(account -> addedCollection.add(new Account(account)), account1, account2, account3, account4);

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

        // Retrieving a non-existent account does not call the consumer.
        final Collection<Account> notFound = new ArrayList<>(1);
        dao.get(notFound::add, 1111L);
        assertTrue(notFound.isEmpty());

        // Looking for a specific account id returns the expected account when it exists.
        final Collection<Account> found = new ArrayList<>(1);
        dao.get(found::add, account1.getId().orElse(null));
        assertEquals(1, found.size());
        assertTrue(found.contains(account1));

        // Looking for multiple account ids returns the expected accounts when they exist.
        final Collection<Account> multiple = new ArrayList<>(1);
        dao.get(account -> multiple.add(new Account(account)), account1.getId().orElse(null),
                account2.getId().orElse(null));
        assertEquals(2, multiple.size());
        assertTrue(multiple.contains(account1));
        assertTrue(multiple.contains(account2));

        // Looking for mixture of missing and available account ids returns only the ones that are available.
        final Collection<Account> mixture = new ArrayList<>(1);
        dao.get(account -> mixture.add(new Account(account)), account1.getId().orElse(null), 2222L);
        assertEquals(1, mixture.size());
        assertTrue(mixture.contains(account1));

        // Retrieving all accounts consumes all of them.
        final Collection<Account> all = new ArrayList<>(1);
        dao.getAll(account -> all.add(new Account(account)));
        assertEquals(4, all.size());
        assertTrue(all.contains(account1));
        assertTrue(all.contains(account2));
        assertTrue(all.contains(account3));
        assertTrue(all.contains(account4));

        // Removing an account that does not exist returns a count of 0.
        assertEquals(0, dao.remove(1111L));

        // Removing a single id that exists returns a count of 1.
        assertEquals(1, dao.remove(account1.getId().orElse(null)));

        // We just removed this account, so now expecting a 0 count.
        assertEquals(0, dao.remove(account1.getId().orElse(null)));

        // Removing a mixture of existing and missing ids returns the correct count.
        assertEquals(3, dao.remove(account2.getId().orElse(null), 2222L, 3333L, account3.getId().orElse(null),
                account4.getId().orElse(null)));
    }

    @Test(expected = DaoException.class)
    public void testGetAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        dao.get(IGNORED, 1111L);
    }

    @Test(expected = DaoException.class)
    public void testGetAllAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        dao.getAll(IGNORED);
    }

    @Test(expected = DaoException.class)
    public void testAddAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        final Account account = new Account("add-account-exception", new ServiceLevel());

        dao.add(IGNORED, account);
    }

    @Test(expected = DaoException.class)
    public void testRemoveAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        dao.remove(1111L);
    }
}
