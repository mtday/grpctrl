package com.grpctrl.db.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.GroupControlDao;
import com.grpctrl.db.error.DaoException;

import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides a base unit test class responsible for testing {@link GroupControlDao} implementations.
 */
public abstract class BaseGroupControlDaoTest {
    /**
     * @return the {@link GroupControlDao} implementation to be tested
     */
    public abstract GroupControlDao getGroupControlDao();

    /**
     * @return the {@link GroupControlDao} implementation to be tested
     */
    public abstract GroupControlDao getGroupControlDaoWithDataSourceException();

    final Tag tag(@Nonnull final String labelAndValue) {
        return new Tag.Builder(labelAndValue, labelAndValue).build();
    }

    @Test
    public void testAccountManagement() throws DaoException {
        final GroupControlDao dao = getGroupControlDao();

        final Account account =
                new Account.Builder("account-management-test", new ServiceLevel.Builder().build()).build();

        final Account added = dao.addAccount(account);

        assertNotNull(added);
        assertTrue(added.getId().isPresent());

        final Optional<Account> missing = dao.getAccount(1111L);
        final Optional<Account> retrieved = dao.getAccount(added.getId().orElse(null));

        assertFalse(missing.isPresent());

        assertTrue(retrieved.isPresent());
        assertEquals(added, retrieved.get());

        final boolean removed = dao.removeAccount(added.getId().orElse(null));
        assertTrue(removed);

        final boolean notRemoved = dao.removeAccount(1111L);
        assertFalse(notRemoved);

        final Optional<Account> gone = dao.getAccount(added.getId().orElse(null));
        assertFalse(gone.isPresent());
    }

    @Test(expected = DaoException.class)
    public void testGetAccountException() throws DaoException {
        final GroupControlDao dao = getGroupControlDaoWithDataSourceException();

        dao.getAccount(1111L);
    }

    @Test(expected = DaoException.class)
    public void testAddAccountException() throws DaoException {
        final GroupControlDao dao = getGroupControlDaoWithDataSourceException();

        final Account account =
                new Account.Builder("add-account-exception", new ServiceLevel.Builder().build()).build();

        dao.addAccount(account);
    }

    @Test(expected = DaoException.class)
    public void testRemoveAccountException() throws DaoException {
        final GroupControlDao dao = getGroupControlDaoWithDataSourceException();

        dao.removeAccount(1111L);
    }

    @Test
    public void testGroupManagement() throws DaoException {
        final GroupControlDao dao = getGroupControlDao();

        final Account account = dao.addAccount(
                new Account.Builder("group-management-test", new ServiceLevel.Builder().build()).build());

        final Group gm = new Group.Builder("m").addTag(tag("m")).build();
        final Group g111 = new Group.Builder("111").addTag(tag("111")).addChild(gm).build();
        final Group g112 = new Group.Builder("112").addTag(tag("112")).addChild(gm).build();
        final Group g11 = new Group.Builder("11").addTag(tag("11")).addChildren(g111, g112, gm).build();
        final Group g121 = new Group.Builder("121").addTag(tag("121")).build();
        final Group g122 = new Group.Builder("122").addTag(tag("122")).build();
        final Group g12 = new Group.Builder("12").addTag(tag("12")).addChildren(g121, g122, gm).build();
        final Group g1 = new Group.Builder("1").addTag(tag("1")).addChildren(g11, g12, gm).build();
        final Group g2 = new Group.Builder("2").addTag(tag("2")).build();
        final Group g = new Group.Builder("").addTag(tag("")).addChildren(g1, g2).build();

        dao.addGroups(account, Collections.singleton(g));
    }
}
