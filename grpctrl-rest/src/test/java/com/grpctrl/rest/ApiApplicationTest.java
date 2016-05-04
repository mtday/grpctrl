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

        final List<String> objects = baseApplication.getInstances().stream().map(Object::getClass).map(Class::getName)
                .collect(Collectors.toList());
        final Iterator<String> objIter = new TreeSet<>(objects).iterator();

        assertEquals("com.grpctrl.common.supplier.ConfigSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.common.supplier.HealthCheckRegistrySupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.common.supplier.MetricRegistrySupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.common.supplier.ObjectMapperSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.crypto.ske.SymmetricKeyEncryptionSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.crypto.ssl.SslContextSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.crypto.store.KeyStoreSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.crypto.store.TrustStoreSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.db.DataSourceSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.AccountDaoSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.GroupDaoSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier$Binder", objIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.TagDaoSupplier$Binder", objIter.next());
        assertFalse(objIter.hasNext());

        final List<String> names =
                baseApplication.getClasses().stream().map(Class::getName).collect(Collectors.toList());
        final Iterator<String> nameIter = new TreeSet<>(names).iterator();

        assertEquals("com.grpctrl.common.supplier.ConfigSupplier", nameIter.next());
        assertEquals("com.grpctrl.common.supplier.HealthCheckRegistrySupplier", nameIter.next());
        assertEquals("com.grpctrl.common.supplier.MetricRegistrySupplier", nameIter.next());
        assertEquals("com.grpctrl.common.supplier.ObjectMapperSupplier", nameIter.next());
        assertEquals("com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier", nameIter.next());
        assertEquals("com.grpctrl.crypto.ske.SymmetricKeyEncryptionSupplier", nameIter.next());
        assertEquals("com.grpctrl.crypto.ssl.SslContextSupplier", nameIter.next());
        assertEquals("com.grpctrl.crypto.store.KeyStoreSupplier", nameIter.next());
        assertEquals("com.grpctrl.crypto.store.TrustStoreSupplier", nameIter.next());
        assertEquals("com.grpctrl.db.DataSourceSupplier", nameIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.AccountDaoSupplier", nameIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.GroupDaoSupplier", nameIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier", nameIter.next());
        assertEquals("com.grpctrl.db.dao.supplier.TagDaoSupplier", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountAdd", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountGet", nameIter.next());
        assertEquals("com.grpctrl.rest.resource.v1.account.AccountGetAll", nameIter.next());
        assertFalse(nameIter.hasNext());
    }
}
