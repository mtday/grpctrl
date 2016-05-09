package com.grpctrl.db.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.Group;
import com.grpctrl.common.model.Tag;
import com.grpctrl.db.dao.AccountDao;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.error.QuotaExceededException;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

/**
 * Provides a base unit test class responsible for testing {@link AccountDao} implementations.
 */
public abstract class BaseGroupDaoTest {
    // A consumer that ignores the groups and tags passed into it.
    private static final BiConsumer<Group, Iterator<Tag>> IGNORED = (group, tagIter) -> {
    };

    // A consumer that ignores the accounts passed into it.
    private static final Consumer<Account> ACCOUNT_IGNORED = (account) -> {
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
    public void testSimpleTopLevelGroupManagement() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account1 = new Account("top-level-account-1");
        final Account account2 = new Account("top-level-account-2");
        getAccountDao().add(asList(account1, account2).iterator(), ACCOUNT_IGNORED);

        final Group a = new Group("simple-a-no-tags");
        final Group b = new Group("simple-b").addTags(new Tag("b", "b1"), new Tag("b", "b2"));
        final Group c = new Group("simple-c").addTags(new Tag("c1", "c1"), new Tag("c2", "c2"));

        // Adding the groups gives back the same groups along with the tags.
        final Collection<Group> added = new ArrayList<>();
        dao.add(account1, asList(a, b, c).iterator(), new AddTo(added));
        assertEquals(3, added.size());
        assertTrue(added.contains(a));
        assertTrue(added.contains(b));
        assertTrue(added.contains(c));

        // Pull out the ids.
        final Long aid = a.getId().orElse(null);
        final Long bid = b.getId().orElse(null);
        final Long cid = c.getId().orElse(null);

        // Exists by id should behave as expected.
        assertTrue(dao.exists(account1, aid));
        assertTrue(dao.exists(account1, bid));
        assertTrue(dao.exists(account1, cid));
        assertFalse(dao.exists(account1, 1111L));
        assertFalse(dao.exists(account2, aid));
        assertFalse(dao.exists(account2, bid));
        assertFalse(dao.exists(account2, cid));

        // Exists by name should behave as expected.
        assertTrue(dao.exists(account1, a.getName()));
        assertTrue(dao.exists(account1, b.getName()));
        assertTrue(dao.exists(account1, c.getName()));
        assertFalse(dao.exists(account1, "missing"));
        assertFalse(dao.exists(account2, a.getName()));
        assertFalse(dao.exists(account2, b.getName()));
        assertFalse(dao.exists(account2, c.getName()));

        // Get should find all the top-level groups for the account.
        final Collection<Group> get = new ArrayList<>();
        dao.get(account1, new AddTo(get));
        assertEquals(3, get.size());
        assertTrue(get.contains(a));
        assertTrue(get.contains(b));
        assertTrue(get.contains(c));

        // Get should not find any top-level groups for the wrong account.
        final Collection<Group> wrongAccount = new ArrayList<>();
        dao.get(account2, new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find top-level groups by id.
        final Collection<Group> byId = new ArrayList<>();
        dao.getById(account1, singleton(aid), new AddTo(byId));
        assertEquals(1, byId.size());
        assertTrue(byId.contains(a));

        // Should NOT be able to find top-level groups by id for the wrong account.
        dao.getById(account2, singleton(aid), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find top-level groups by name.
        final Collection<Group> byName = new ArrayList<>();
        dao.getByName(account1, singleton(a.getName()), new AddTo(byName));
        assertEquals(1, byName.size());
        assertTrue(byName.contains(a));

        // Should NOT be able to find top-level groups by name for the wrong account.
        dao.getByName(account2, singleton(a.getName()), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find top-level groups by regular expression, case sensitive.
        final Collection<Group> byRegexSensitive = new ArrayList<>();
        dao.find(account1, singleton("^sim"), true, new AddTo(byRegexSensitive));
        assertEquals(3, byRegexSensitive.size());
        assertTrue(byRegexSensitive.containsAll(asList(a, b, c)));

        // Should be able to find top-level groups by regular expression, case insensitive.
        final Collection<Group> byRegexInsensitive = new ArrayList<>();
        dao.find(account1, singleton("PLE-*"), false, new AddTo(byRegexInsensitive));
        assertEquals(3, byRegexInsensitive.size());
        assertTrue(byRegexInsensitive.containsAll(asList(a, b, c)));

        // Should NOT be able to find top-level groups by regex for the wrong account.
        dao.find(account2, singleton(".*"), false, new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // None of these groups have any children, so looking for children won't find any.
        final Collection<Group> children = new ArrayList<>();
        final AddTo childAdder = new AddTo(children);
        dao.childrenById(account1, singleton(aid), childAdder);
        dao.childrenById(account1, singleton(bid), childAdder);
        dao.childrenById(account1, singleton(cid), childAdder);
        dao.childrenById(account2, singleton(aid), childAdder);
        dao.childrenById(account2, singleton(bid), childAdder);
        dao.childrenById(account2, singleton(cid), childAdder);
        dao.childrenByName(account1, singleton(a.getName()), childAdder);
        dao.childrenByName(account1, singleton(b.getName()), childAdder);
        dao.childrenByName(account1, singleton(c.getName()), childAdder);
        dao.childrenByName(account2, singleton(a.getName()), childAdder);
        dao.childrenByName(account2, singleton(b.getName()), childAdder);
        dao.childrenByName(account2, singleton(c.getName()), childAdder);
        dao.childrenFind(account2, singleton(".*"), false, childAdder);
        assertTrue(children.isEmpty());

        // Removing a single account returns the correct count.
        assertEquals(1, dao.remove(account1, singleton(aid)));
        // Removing the account again returns 0 since the account was not found.
        assertEquals(0, dao.remove(account1, singleton(aid)));
        // Removing using the wrong account makes no changes.
        assertEquals(0, dao.remove(account2, singleton(bid)));
        // Removing multiple accounts with a mixture of missing and existing ids returns the correct count.
        assertEquals(2, dao.remove(account1, asList(aid, 1111L, bid, 2222L, cid, 3333L, 4444L)));
    }

    @Test
    public void testSimpleMidLevelGroupManagement() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account1 = new Account("mid-level-account-1");
        final Account account2 = new Account("mid-level-account-2");
        getAccountDao().add(asList(account1, account2).iterator(), ACCOUNT_IGNORED);

        final Group p1 = new Group("parent-1");
        final Group p2 = new Group("parent-2");
        dao.add(account1, asList(p1, p2).iterator(), IGNORED);
        final Long p1id = p1.getId().orElse(null);
        final Long p2id = p2.getId().orElse(null);

        final Group a1 = new Group("simple-a-no-tags");
        final Group b1 = new Group("simple-b").addTags(new Tag("b", "b1"), new Tag("b", "b2"));
        final Group c1 = new Group("simple-c").addTags(new Tag("c1", "c1"), new Tag("c2", "c2"));
        final Group a2 = new Group("simple-a-no-tags");
        final Group b2 = new Group("simple-b").addTags(new Tag("b", "b1"), new Tag("b", "b2"));
        final Group c2 = new Group("simple-c").addTags(new Tag("c1", "c1"), new Tag("c2", "c2"));

        final Group a1a = new Group("child-1-of-a1");
        final Group a1b = new Group("child-2-of-a1").addTags(new Tag("b", "b1"), new Tag("b", "b2"));
        final Group a2a = new Group("child-1-of-a2");
        final Group a2b = new Group("child-2-of-a2").addTags(new Tag("b", "b1"), new Tag("b", "b2"));

        // Adding the groups gives back the same groups along with the tags.
        final Collection<Group> added = new ArrayList<>();
        dao.add(account1, p1id, asList(a1, b1, c1).iterator(), new AddTo(added));
        dao.add(account1, p2id, asList(a2, b2, c2).iterator(), new AddTo(added));
        assertEquals(6, added.size());
        assertTrue(added.containsAll(asList(a1, b1, c1, a2, b2, c2)));
        assertEquals(p1id, a1.getParentId().orElse(null));
        assertEquals(p1id, b1.getParentId().orElse(null));
        assertEquals(p1id, c1.getParentId().orElse(null));
        assertEquals(p2id, a2.getParentId().orElse(null));
        assertEquals(p2id, b2.getParentId().orElse(null));
        assertEquals(p2id, c2.getParentId().orElse(null));

        // Pull out the ids.
        final Long a1id = a1.getId().orElse(null);
        final Long b1id = b1.getId().orElse(null);
        final Long c1id = c1.getId().orElse(null);
        final Long a2id = a2.getId().orElse(null);
        final Long b2id = b2.getId().orElse(null);
        final Long c2id = c2.getId().orElse(null);

        // Add the 3rd level children.
        final Collection<Group> thirds = new ArrayList<>();
        dao.add(account1, a1id, asList(a1a, a1b).iterator(), new AddTo(thirds));
        dao.add(account1, a2id, asList(a2a, a2b).iterator(), new AddTo(thirds));
        assertEquals(4, thirds.size());
        assertTrue(thirds.containsAll(asList(a1a, a1b, a2a, a2b)));
        assertEquals(a1id, a1a.getParentId().orElse(null));
        assertEquals(a1id, a1b.getParentId().orElse(null));
        assertEquals(a2id, a2a.getParentId().orElse(null));
        assertEquals(a2id, a2b.getParentId().orElse(null));

        // Pull out the ids.
        final Long a1aid = a1a.getId().orElse(null);
        final Long a1bid = a1b.getId().orElse(null);
        final Long a2aid = a2a.getId().orElse(null);
        final Long a2bid = a2b.getId().orElse(null);

        // Exists by id should behave as expected.
        assertTrue(dao.exists(account1, a1id));
        assertTrue(dao.exists(account1, b1id));
        assertTrue(dao.exists(account1, c1id));
        assertTrue(dao.exists(account1, a2id));
        assertTrue(dao.exists(account1, b2id));
        assertTrue(dao.exists(account1, c2id));
        assertTrue(dao.exists(account1, a1aid));
        assertTrue(dao.exists(account1, a1bid));
        assertTrue(dao.exists(account1, a2aid));
        assertTrue(dao.exists(account1, a2bid));
        assertFalse(dao.exists(account1, 1111L));
        assertFalse(dao.exists(account2, a1id));
        assertFalse(dao.exists(account2, b1id));
        assertFalse(dao.exists(account2, c1id));
        assertFalse(dao.exists(account2, a2id));
        assertFalse(dao.exists(account2, b2id));
        assertFalse(dao.exists(account2, c2id));
        assertFalse(dao.exists(account2, a1aid));
        assertFalse(dao.exists(account2, a1bid));
        assertFalse(dao.exists(account2, a2aid));
        assertFalse(dao.exists(account2, a2bid));

        // Exists by name should behave as expected.
        assertTrue(dao.exists(account1, a1.getName()));
        assertTrue(dao.exists(account1, b1.getName()));
        assertTrue(dao.exists(account1, c1.getName()));
        assertTrue(dao.exists(account1, a2.getName()));
        assertTrue(dao.exists(account1, b2.getName()));
        assertTrue(dao.exists(account1, c2.getName()));
        assertTrue(dao.exists(account1, a1a.getName()));
        assertTrue(dao.exists(account1, a1b.getName()));
        assertTrue(dao.exists(account1, a2a.getName()));
        assertTrue(dao.exists(account1, a2b.getName()));
        assertFalse(dao.exists(account1, "missing"));
        assertFalse(dao.exists(account2, a1.getName()));
        assertFalse(dao.exists(account2, b1.getName()));
        assertFalse(dao.exists(account2, c1.getName()));
        assertFalse(dao.exists(account2, a2.getName()));
        assertFalse(dao.exists(account2, b2.getName()));
        assertFalse(dao.exists(account2, c2.getName()));
        assertFalse(dao.exists(account2, a1a.getName()));
        assertFalse(dao.exists(account2, a1b.getName()));
        assertFalse(dao.exists(account2, a2a.getName()));
        assertFalse(dao.exists(account2, a2b.getName()));

        // Get should find all the top-level groups for the account.
        final Collection<Group> topLevel = new ArrayList<>();
        dao.get(account1, new AddTo(topLevel));
        assertEquals(2, topLevel.size());
        assertTrue(topLevel.containsAll(asList(p1, p2)));

        // Get should not find any top-level groups for the wrong account.
        final Collection<Group> wrongAccount = new ArrayList<>();
        dao.get(account2, new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find mid-level groups by id.
        final Collection<Group> midById = new ArrayList<>();
        dao.getById(account1, singleton(a1id), new AddTo(midById));
        assertEquals(1, midById.size());
        assertTrue(midById.contains(a1));

        // Should NOT be able to find mid-level groups by id for the wrong account.
        dao.getById(account2, singleton(a1id), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find low-level groups by id.
        final Collection<Group> lowById = new ArrayList<>();
        dao.getById(account1, singleton(a1aid), new AddTo(lowById));
        assertEquals(1, lowById.size());
        assertTrue(lowById.contains(a1a));

        // Should NOT be able to find low-level groups by id for the wrong account.
        dao.getById(account2, singleton(a1aid), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find mid-level groups by name.
        final Collection<Group> midByName = new ArrayList<>();
        dao.getByName(account1, singleton(a1.getName()), new AddTo(midByName));
        assertEquals(2, midByName.size());
        assertTrue(midByName.containsAll(asList(a1, a2)));

        // Should NOT be able to find mid-level groups by name for the wrong account.
        dao.getByName(account2, singleton(a1.getName()), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find low-level groups by name.
        final Collection<Group> lowByName = new ArrayList<>();
        dao.getByName(account1, singleton(a1a.getName()), new AddTo(lowByName));
        assertEquals(1, lowByName.size());
        assertTrue(lowByName.containsAll(singleton(a1a)));

        // Should NOT be able to find low-level groups by name for the wrong account.
        dao.getByName(account2, singleton(a1a.getName()), new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find mid-level groups by regular expression, case sensitive.
        final Collection<Group> midByRegexSensitive = new ArrayList<>();
        dao.find(account1, singleton("^sim"), true, new AddTo(midByRegexSensitive));
        assertEquals(6, midByRegexSensitive.size());
        assertTrue(midByRegexSensitive.containsAll(asList(a1, b1, c1, a2, b2, c2)));

        // Should be able to find mid-level groups by regular expression, case insensitive.
        final Collection<Group> midByRegexInsensitive = new ArrayList<>();
        dao.find(account1, singleton("PLE-*"), false, new AddTo(midByRegexInsensitive));
        assertEquals(6, midByRegexInsensitive.size());
        assertTrue(midByRegexInsensitive.containsAll(asList(a1, b1, c1, a2, b2, c2)));

        // Should be able to find low-level groups by regular expression, case sensitive.
        final Collection<Group> lowByRegexSensitive = new ArrayList<>();
        dao.find(account1, singleton("^chi"), true, new AddTo(lowByRegexSensitive));
        assertEquals(4, lowByRegexSensitive.size());
        assertTrue(lowByRegexSensitive.containsAll(asList(a1a, a1b, a2a, a2b)));

        // Should be able to find low-level groups by regular expression, case insensitive.
        final Collection<Group> lowByRegexInsensitive = new ArrayList<>();
        dao.find(account1, singleton("ILD-*"), false, new AddTo(lowByRegexInsensitive));
        assertEquals(4, lowByRegexInsensitive.size());
        assertTrue(lowByRegexSensitive.containsAll(asList(a1a, a1b, a2a, a2b)));

        // Should NOT be able to find mid- or low-level groups by regex for the wrong account.
        dao.find(account2, singleton(".*"), false, new AddTo(wrongAccount));
        assertTrue(wrongAccount.isEmpty());

        // Should be able to find the children for the top-level groups by id.
        final Collection<Group> topChildrenById = new ArrayList<>();
        dao.childrenById(account1, singleton(p1id), new AddTo(topChildrenById));
        assertEquals(3, topChildrenById.size());
        assertTrue(topChildrenById.containsAll(asList(a1, b1, c1)));

        // Should be able to find the children for the mid-level groups by id.
        final Collection<Group> midChildrenById = new ArrayList<>();
        dao.childrenById(account1, asList(a1id, b1id, c1id), new AddTo(midChildrenById));
        assertEquals(2, midChildrenById.size());
        assertTrue(midChildrenById.containsAll(asList(a1a, a1b)));

        // Should be able to find the children of top-level groups by name.
        final Collection<Group> topChildrenByName = new ArrayList<>();
        dao.childrenByName(account1, singleton(p1.getName()), new AddTo(topChildrenByName));
        assertEquals(3, topChildrenByName.size());
        assertTrue(topChildrenByName.containsAll(asList(a1, b1, c1)));

        // Should be able to find the children of mid-level groups by name.
        final Collection<Group> midChildrenByName = new ArrayList<>();
        dao.childrenByName(account1, singleton(a1.getName()), new AddTo(midChildrenByName));
        assertEquals(4, midChildrenByName.size());
        assertTrue(midChildrenByName.containsAll(asList(a1a, a1b, a2a, a2b)));

        // Should be able to find the children for the top-level groups by regex, case insensitive.
        final Collection<Group> topChildrenByRegex = new ArrayList<>();
        dao.childrenFind(account1, singleton("^parent"), false, new AddTo(topChildrenByRegex));
        assertEquals(6, topChildrenByRegex.size());
        assertTrue(topChildrenByRegex.containsAll(asList(a1, b1, c1, a2, b2, c2)));

        // Should be able to find the children for the mid-level groups by regex, case insensitive.
        final Collection<Group> midChildrenByRegex = new ArrayList<>();
        dao.childrenFind(account1, singleton("^simp"), false, new AddTo(midChildrenByRegex));
        assertEquals(4, midChildrenByRegex.size());
        assertTrue(midChildrenByRegex.containsAll(asList(a1a, a1b, a2a, a2b)));

        // Removing a top-level group recursively deletes its children.
        // (Note that cascade-deleted rows are not included in the count.)
        assertEquals(1, dao.remove(account1, singleton(p1id)));
        assertEquals(0, dao.remove(account1, asList(a1id, b1id, c1id, a1aid, a1bid)));

        // Removing a mid-level group recursively deletes its children.
        assertEquals(1, dao.remove(account1, singleton(a2id)));
        assertEquals(0, dao.remove(account1, asList(a2aid, a2bid)));

        // Deleting an account also deletes the groups in the account.
        assertEquals(1, getAccountDao().remove(singleton(account1.getId().orElse(null))));
        assertEquals(0, dao.remove(account1, singleton(p2id)));
    }

    @Test(expected = QuotaExceededException.class)
    public void testGroupsExceedQuotaIndividualInserts() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("groups-quota-individual-account-1");
        account.getServiceLevel().setMaxGroups(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        for (int i = 0; i <= account.getServiceLevel().getMaxGroups(); i++) {
            final Group group = new Group("group-number-" + i);
            dao.add(account, singleton(group).iterator(), IGNORED);
        }
    }

    @Test(expected = QuotaExceededException.class)
    public void testGroupsExceedQuotaBatchInserts() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("groups-quota-batch-account-1");
        account.getServiceLevel().setMaxGroups(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Collection<Group> groups = new ArrayList<>(account.getServiceLevel().getMaxGroups());
        for (int i = 0; i <= account.getServiceLevel().getMaxGroups(); i++) {
            groups.add(new Group("group-number-" + i));
        }
        dao.add(account, groups.iterator(), IGNORED);
    }

    @Test(expected = QuotaExceededException.class)
    public void testGroupsExceedQuotaBatchInsertsAsChildren() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("groups-quota-children-account-1");
        account.getServiceLevel().setMaxGroups(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Group parent = new Group("group-number-1");
        dao.add(account, singleton(parent).iterator(), IGNORED);
        final Long parentId = parent.getId().orElse(null);

        final Collection<Group> groups = new ArrayList<>(account.getServiceLevel().getMaxGroups());
        // Starting at 1 since the parent has already been inserted.
        for (int i = 1; i <= account.getServiceLevel().getMaxGroups(); i++) {
            groups.add(new Group("group-number-" + i));
        }
        dao.add(account, parentId, groups.iterator(), IGNORED);
    }

    @Test(expected = QuotaExceededException.class)
    public void testTagsExceedQuotaSingleGroup() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("tags-quota-single-account-1");
        account.getServiceLevel().setMaxTags(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Group group = new Group("group");
        for (int i = 0; i <= account.getServiceLevel().getMaxTags(); i++) {
            group.addTags(new Tag("tag-" + i, "value"));
        }
        dao.add(account, singleton(group).iterator(), IGNORED);
    }

    @Test(expected = QuotaExceededException.class)
    public void testTagsExceedQuotaMultipleGroups() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("tags-quota-multiple-account-1");
        account.getServiceLevel().setMaxTags(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        for (int i = 0; i <= account.getServiceLevel().getMaxTags(); i++) {
            final Group group = new Group("group-" + i);
            group.addTags(new Tag("tag-" + i, "value"));
            dao.add(account, singleton(group).iterator(), IGNORED);
        }
    }

    @Test(expected = QuotaExceededException.class)
    public void testDepthExceedsQuota() {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("depth-quota-account-1");
        account.getServiceLevel().setMaxDepth(3);
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        Long parentId = null;
        for (int i = 0; i <= account.getServiceLevel().getMaxDepth(); i++) {
            final Group group = new Group("depth-" + i);
            dao.add(account, parentId, singleton(group).iterator(), IGNORED);
            parentId = group.getId().orElse(null);
        }
    }

    @Test(expected = BadRequestException.class)
    public void testTopLevelGroupsWithSameNameWithSameParentNotAllowed() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("same-name");
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Group a = new Group("same-name");
        final Group b = new Group("same-name").addTags(new Tag("l", "v"));

        dao.add(account, asList(a, b).iterator(), IGNORED);
    }

    @Test(expected = BadRequestException.class)
    public void testMidLevelGroupsWithSameNameWithSameParentNotAllowed() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("same-name");
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Group parent = new Group("parent");
        dao.add(account, singleton(parent).iterator(), IGNORED);

        final Group a = new Group("same-name");
        final Group b = new Group("same-name").addTags(new Tag("l", "v"));

        dao.add(account, parent.getId().orElse(null), asList(a, b).iterator(), IGNORED);
    }

    @Test
    public void testDifferentAccountsSameGroupName() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account1 = new Account("name-test-account-1");
        final Account account2 = new Account("name-test-account-2");
        getAccountDao().add(asList(account1, account2).iterator(), ACCOUNT_IGNORED);

        final Group a = new Group("same-name");
        final Group b = new Group("same-name");

        dao.add(account1, singleton(a).iterator(), IGNORED);
        dao.add(account2, singleton(b).iterator(), IGNORED);

        final long aid = a.getId().orElse(null);
        final long bid = b.getId().orElse(null);

        assertNotEquals(aid, bid);

        dao.getByName(account1, singleton(a.getName()), (group, tagIter) -> assertEquals(a, group));
        dao.getByName(account2, singleton(b.getName()), (group, tagIter) -> assertEquals(b, group));
    }

    @Test
    public void testDifferentLevelsSameGroupName() throws WebApplicationException {
        final GroupDao dao = getGroupDao();

        final Account account = new Account("level-name-test-account-1");
        getAccountDao().add(singleton(account).iterator(), ACCOUNT_IGNORED);

        final Group parent = new Group("same-name");
        final Group child = new Group("same-name");

        dao.add(account, singleton(parent).iterator(), IGNORED);
        dao.add(account, parent.getId().orElse(null), singleton(child).iterator(), IGNORED);

        final long pid = parent.getId().orElse(null);
        final long cid = child.getId().orElse(null);

        assertNotEquals(pid, cid);
        assertEquals(new Long(pid), child.getParentId().orElse(null));

        final Collection<Group> both = new ArrayList<>();
        dao.getByName(account, singleton(parent.getName()), new AddTo(both));
        assertEquals(2, both.size());
        assertTrue(both.containsAll(asList(parent, child)));
    }

    @Test(expected = BadRequestException.class)
    public void testAddGroupToParentInDifferentAccount() {
        final GroupDao dao = getGroupDao();

        final Account account1 = new Account("parent-test-account-1");
        final Account account2 = new Account("parent-test-account-2");
        getAccountDao().add(asList(account1, account2).iterator(), ACCOUNT_IGNORED);

        final Group parent = new Group("parent");
        dao.add(account1, singleton(parent).iterator(), IGNORED);

        final Group child = new Group("child");

        dao.add(account2, parent.getId().orElse(null), singleton(child).iterator(), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testExistsByIdException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().exists(new Account("exception-account"), 1L);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testExistsByNameException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().exists(new Account("exception-account"), "name");
    }

    @Test(expected = InternalServerErrorException.class)
    public void testGetException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().get(new Account("exception-account"), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testGetByIdException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().getById(new Account("exception-account"), singleton(1L), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testGetByNameException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().getByName(new Account("exception-account"), singleton("name"), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testChildrenByIdException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().childrenById(new Account("exception-account"), singleton(1L), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testChildrenByNameException() throws WebApplicationException {
        getGroupDaoWithDataSourceException()
                .childrenByName(new Account("exception-account"), singleton("name"), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testAddException() throws WebApplicationException {
        getGroupDaoWithDataSourceException()
                .add(new Account("exception-account"), singleton(new Group("group")).iterator(), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testAddWithParentIdException() throws WebApplicationException {
        getGroupDaoWithDataSourceException()
                .add(new Account("exception-account"), 1L, singleton(new Group("group")).iterator(), IGNORED);
    }

    @Test(expected = InternalServerErrorException.class)
    public void testRemoveException() throws WebApplicationException {
        getGroupDaoWithDataSourceException().remove(new Account("exception-account"), singleton(1111L));
    }

    // A consumer that adds groups to a collection.
    private static class AddTo implements BiConsumer<Group, Iterator<Tag>> {
        @Nonnull
        private final Collection<Group> collection;

        public AddTo(@Nonnull final Collection<Group> collection) {
            this.collection = Objects.requireNonNull(collection);
        }

        @Override
        public void accept(@Nonnull final Group group, @Nonnull final Iterator<Tag> tagIterator) {
            this.collection.add(new Group(group, tagIterator));
        }
    }
}
