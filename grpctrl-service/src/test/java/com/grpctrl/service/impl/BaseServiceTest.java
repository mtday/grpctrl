package com.grpctrl.service.impl;

import static org.junit.Assert.assertFalse;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ServiceLevel;
import com.grpctrl.service.GroupControlService;

import org.junit.Test;

/**
 * Provides a base class capable of testing a {@link GroupControlService} implementation.
 */
public abstract class BaseServiceTest {
    public abstract GroupControlService getService();

    public Account getAccount() {
        return new Account.Builder("test-account", new ServiceLevel.Builder().build()).build();
    }

    @Test
    public void testExistsDoesNotExist() {
        assertFalse(getService().exists(getAccount(), 1L));
    }
}
