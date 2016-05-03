package com.grpctrl.rest;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.HealthCheckRegistrySupplier;
import com.grpctrl.common.supplier.MetricRegistrySupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ske.SymmetricKeyEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.crypto.store.TrustStoreSupplier;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.GroupControlDaoSupplier;
import com.grpctrl.rest.resource.v1.account.AccountAdd;
import com.grpctrl.rest.resource.v1.account.AccountGet;

import org.glassfish.jersey.server.ResourceConfig;

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
        register(new PasswordBasedEncryptionSupplier.Binder());
        register(new KeyStoreSupplier.Binder());
        register(new TrustStoreSupplier.Binder());
        register(new SymmetricKeyEncryptionSupplier.Binder());
        register(new SslContextSupplier.Binder());
        register(new ObjectMapperSupplier.Binder());
        register(new MetricRegistrySupplier.Binder());
        register(new HealthCheckRegistrySupplier.Binder());
        register(new DataSourceSupplier.Binder());
        register(new GroupControlDaoSupplier.Binder());

        // This is to register the ContextResolver for these classes so Jersey uses them by default also. For example,
        // without the ObjectMapperSupplier, jersey would use it's own ObjectMapper to serialize responses to JSON,
        // even though it would be injecting our supplier objects into our classes. This only matters for ObjectMapper
        // (since jersey only uses ObjectMapper and not the others) but doing the others for consistency.
        register(ConfigSupplier.class);
        register(PasswordBasedEncryptionSupplier.class);
        register(KeyStoreSupplier.class);
        register(TrustStoreSupplier.class);
        register(SymmetricKeyEncryptionSupplier.class);
        register(SslContextSupplier.class);
        register(ObjectMapperSupplier.class);
        register(MetricRegistrySupplier.class);
        register(HealthCheckRegistrySupplier.class);
        register(DataSourceSupplier.class);
        register(GroupControlDaoSupplier.class);

        // Resource classes.
        register(AccountAdd.class);
        register(AccountGet.class);
    }
}
