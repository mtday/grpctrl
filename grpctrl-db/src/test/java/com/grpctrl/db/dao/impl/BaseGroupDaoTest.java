package com.grpctrl.db.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterators;
import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.error.DaoException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Provides a base unit test class responsible for testing {@link AccountDao} implementations.
 */
public abstract class BaseGroupDaoTest {
    // A consumer that ignores the groups and tags passed into it.
    private static final BiConsumer<Group, Iterator<Tag>> IGNORED = (group, tagIter) -> {
    };

    /**
     * @return the {@link AccountDao} implementation to use during testing of groups
     */
    public abstract AccountDao getAccountDao();

    /**
     * @return the {@link GroupDao} implementation to be tested
     */
    public abstract GroupDao getGroupDao();

    /**
     * @return the {@link GroupDao} implementation to be tested
     */
    public abstract GroupDao getGroupDaoWithDataSourceException();

    @Test
    public void testGroupManagement() throws DaoException {
        final GroupDao dao = getGroupDao();

        final Account account1 = new Account("account-1");
        final Account account2 = new Account("account-2");
        getAccountDao().add(account -> {
        }, account1, account2);

        final Group a = new Group("a").addTags(new Tag("a", "a1"), new Tag("a", "a2"));
        final Group b = new Group("b").addTags(new Tag("b1", "b1"), new Tag("b2", "b2"));

        // Adding the groups gives back the same groups along with the tags.
        final Collection<Group> added = new ArrayList<>();
        final Collection<Tag> addedTags = new ArrayList<>();
        dao.add((group, tagIter) -> {
            added.add(new Group(group));
            Iterators.addAll(addedTags, tagIter);
        }, account1, Arrays.asList(a, b));

        assertEquals(2, added.size());
        assertTrue(added.contains(a));
        assertTrue(added.contains(b));

        assertEquals(4, addedTags.size());
        assertTrue(addedTags.containsAll(a.getTags()));
        assertTrue(addedTags.containsAll(b.getTags()));
    }
}
