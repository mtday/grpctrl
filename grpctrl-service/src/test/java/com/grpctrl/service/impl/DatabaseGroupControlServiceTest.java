package com.grpctrl.service.impl;

import com.grpctrl.service.GroupControlService;

/**
 * Perform testing on the {@link DatabaseGroupControlService} class.
 */
public class DatabaseGroupControlServiceTest extends BaseServiceTest {
    private static GroupControlService service = new DatabaseGroupControlService();

    @Override
    public GroupControlService getService() {
        return service;
    }
}
