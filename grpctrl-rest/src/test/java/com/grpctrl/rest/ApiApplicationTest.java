package com.grpctrl.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Perform testing on the {@link ApiApplication} class.
 */
public class ApiApplicationTest {
    @Test
    public void test() {
        final ApiApplication baseApplication = new ApiApplication();

        final List<String> names =
                baseApplication.getClasses().stream().map(Class::getName).collect(Collectors.toList());
        final Iterator<String> nameIter = new TreeSet<>(names).iterator();

        assertEquals("com.grpctrl.common.supplier.ObjectMapperSupplier", nameIter.next());
        assertEquals("com.grpctrl.rest.providers.GenericExceptionMapper", nameIter.next());
        assertEquals("com.grpctrl.rest.providers.MemoryUsageLogger", nameIter.next());
        assertEquals("com.grpctrl.rest.providers.RequestLoggingFilter", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.auth.Login", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.auth.Logout", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountAdd", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountGet", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountGetAll", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountRemove", nameIter.next());
        assertEquals("org.glassfish.jersey.message.GZipEncoder", nameIter.next());
        assertEquals("org.glassfish.jersey.server.filter.EncodingFilter", nameIter.next());
        assertEquals("org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature", nameIter.next());
        assertFalse(nameIter.hasNext());
    }
}
