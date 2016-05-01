package com.grpctrl.db.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.db.GroupControlDao;
import com.grpctrl.db.error.DaoException;

import org.junit.Test;

import java.util.Optional;

/**
 * Provides a base unit test class responsible for testing {@link GroupControlDao} implementations.
 */
public abstract class BaseGroupControlDaoTest {
    /**
     * @return the {@link GroupControlDao} implementation to be tested
     */
    public abstract GroupControlDao getGroupControlDao();

    @Test
    public void testAccountManagement() throws DaoException {
        final GroupControlDao dao = getGroupControlDao();

        final Account account =
                new Account.Builder("account-management-test", new ServiceLevel.Builder().build()).build();

        dao.addAccount(account);

        final Optional<Account> missing = dao.getAccount("does-not-exist");
        final Optional<Account> retrieved = dao.getAccount(account.getId());

        assertFalse(missing.isPresent());

        assertTrue(retrieved.isPresent());
        assertEquals(account, retrieved.get());

        dao.removeAccount(account.getId());

        final Optional<Account> gone = dao.getAccount(account.getId());
        assertFalse(gone.isPresent());
    }
}
