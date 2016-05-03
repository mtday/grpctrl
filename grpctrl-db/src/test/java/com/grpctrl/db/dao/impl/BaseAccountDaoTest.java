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
import java.util.Optional;

/**
 * Provides a base unit test class responsible for testing {@link AccountDao} implementations.
 */
public abstract class BaseAccountDaoTest {
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

        final Account account =
                new Account.Builder("account-management-test", new ServiceLevel.Builder().build()).build();

        final Collection<Account> addedCollection = new ArrayList<>(1);
        dao.add(addedCollection::add, account);
        assertEquals(1, addedCollection.size());

        final Account added = addedCollection.iterator().next();
        final Optional<Long> addedId = added.getId();
        assertTrue(addedId.isPresent());

        final Collection<Account> missing = new ArrayList<>(1);
        dao.get(missing::add, 1111L);
        assertTrue(missing.isEmpty());

        final Collection<Account> found = new ArrayList<>(1);
        dao.get(found::add, addedId.get());
        assertEquals(1, found.size());
        assertTrue(found.contains(added));

        assertEquals(1, dao.remove(addedId.get()));
        assertEquals(0, dao.remove(1111L));

        dao.get(missing::add, addedId.get());
        assertTrue(missing.isEmpty());
    }

    @Test(expected = DaoException.class)
    public void testGetAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        dao.get(account -> { }, 1111L);
    }

    @Test(expected = DaoException.class)
    public void testAddAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        final Account account =
                new Account.Builder("add-account-exception", new ServiceLevel.Builder().build()).build();

        dao.add(added -> { }, account);
    }

    @Test(expected = DaoException.class)
    public void testRemoveAccountException() throws DaoException {
        final AccountDao dao = getAccountDaoWithDataSourceException();

        dao.remove(1111L);
    }
}
