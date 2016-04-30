package com.grpctrl.service.impl;

import com.grpctrl.service.GroupControlService;

/**
 * Perform testing on the {@link MemoryGroupControlService} class.
 */
public class MemoryGroupControlServiceTest extends BaseServiceTest {
    private static GroupControlService service = new MemoryGroupControlService();

    @Override
    public GroupControlService getService() {
        return service;
    }
}
