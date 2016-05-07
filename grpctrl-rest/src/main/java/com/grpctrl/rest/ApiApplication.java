package com.grpctrl.rest;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.HealthCheckRegistrySupplier;
import com.grpctrl.common.supplier.MetricRegistrySupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.common.supplier.ScheduledExecutorServiceSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ske.SymmetricKeyEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;
import com.grpctrl.db.dao.supplier.GroupDaoSupplier;
import com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier;
import com.grpctrl.db.dao.supplier.TagDaoSupplier;
import com.grpctrl.rest.providers.GenericExceptionMapper;
import com.grpctrl.rest.providers.MemoryUsageLogger;
import com.grpctrl.rest.providers.RequestLoggingFilter;
import com.grpctrl.rest.resource.v1.account.AccountAdd;
import com.grpctrl.rest.resource.v1.account.AccountGet;
import com.grpctrl.rest.resource.v1.account.AccountGetAll;
import com.grpctrl.rest.resource.v1.account.AccountRemove;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import javax.ws.rs.ApplicationPath;

/**
 * Provides the infrastructure (dependency injection and configuration) for the Jersey REST-based application that
 * hosts the API for this system.
 */
@ApplicationPath("/")
public class ApiApplication extends ResourceConfig {
    /**
     * Default constructor.
     */
    public ApiApplication() {
        register(new ConfigSupplier.Binder());
        register(new ScheduledExecutorServiceSupplier.Binder());
        register(new PasswordBasedEncryptionSupplier.Binder());
        register(new KeyStoreSupplier.Binder());
        register(new SymmetricKeyEncryptionSupplier.Binder());
        register(new SslContextSupplier.Binder());
        register(new ObjectMapperSupplier.Binder());
        register(new MetricRegistrySupplier.Binder());
        register(new HealthCheckRegistrySupplier.Binder());
        register(new DataSourceSupplier.Binder());
        register(new AccountDaoSupplier.Binder());
        register(new GroupDaoSupplier.Binder());
        register(new ServiceLevelDaoSupplier.Binder());
        register(new TagDaoSupplier.Binder());

        // So Jersey will use our ObjectMapper configuration.
        register(ObjectMapperSupplier.class);

        // Resource classes.
        register(AccountAdd.class);
        register(AccountRemove.class);
        register(AccountGet.class);
        register(AccountGetAll.class);

        // Log requests as they come in.
        register(RequestLoggingFilter.class);

        // Log memory usage stats.
        register(MemoryUsageLogger.class);

        // Handle exceptions.
        register(GenericExceptionMapper.class);

        // Turn on gzip compression.
        EncodingFilter.enableFor(this, GZipEncoder.class);
    }
}
