package com.grpctrl.run;

import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneConstant;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.bind;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.ExecutorServiceSupplier;
import com.grpctrl.common.supplier.HealthCheckRegistrySupplier;
import com.grpctrl.common.supplier.MetricRegistrySupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.common.supplier.ScheduledExecutorServiceSupplier;
import com.grpctrl.crypto.pbe.PasswordBasedEncryptionSupplier;
import com.grpctrl.crypto.ske.SymmetricKeyEncryptionSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;
import com.grpctrl.crypto.store.KeyStoreSupplier;
import com.grpctrl.db.DataSourceSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;
import com.grpctrl.db.dao.supplier.ApiLoginDaoSupplier;
import com.grpctrl.db.dao.supplier.GroupDaoSupplier;
import com.grpctrl.db.dao.supplier.ServiceLevelDaoSupplier;
import com.grpctrl.db.dao.supplier.TagDaoSupplier;
import com.grpctrl.db.dao.supplier.UserAuthDaoSupplier;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;
import com.grpctrl.db.dao.supplier.UserEmailDaoSupplier;
import com.grpctrl.db.dao.supplier.UserRoleDaoSupplier;
import com.grpctrl.security.CustomLoginServiceSupplier;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import javax.annotation.Nonnull;

/**
 * Manages service injection.
 */
public class InjectionManager {
    @Nonnull
    private final ServiceLocator serviceLocator;

    public InjectionManager() {
        this.serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        addOneConstant(this.serviceLocator, this);
        bind(this.serviceLocator, new ConfigSupplier.Binder());
        bindAll();
    }

    public InjectionManager(@Nonnull final ConfigSupplier configSupplier) {
        this.serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        addOneConstant(this.serviceLocator, this);
        addOneConstant(this.serviceLocator, configSupplier);
        bindAll();
    }

    private void bindAll() {
        bind(this.serviceLocator, new ExecutorServiceSupplier.Binder());
        bind(this.serviceLocator, new ScheduledExecutorServiceSupplier.Binder());
        bind(this.serviceLocator, new PasswordBasedEncryptionSupplier.Binder());
        bind(this.serviceLocator, new KeyStoreSupplier.Binder());
        bind(this.serviceLocator, new SymmetricKeyEncryptionSupplier.Binder());
        bind(this.serviceLocator, new SslContextSupplier.Binder());
        bind(this.serviceLocator, new ObjectMapperSupplier.Binder());
        bind(this.serviceLocator, new OAuth20ServiceSupplier.Binder());
        bind(this.serviceLocator, new MetricRegistrySupplier.Binder());
        bind(this.serviceLocator, new HealthCheckRegistrySupplier.Binder());
        bind(this.serviceLocator, new DataSourceSupplier.Binder());
        bind(this.serviceLocator, new AccountDaoSupplier.Binder());
        bind(this.serviceLocator, new ApiLoginDaoSupplier.Binder());
        bind(this.serviceLocator, new GroupDaoSupplier.Binder());
        bind(this.serviceLocator, new ServiceLevelDaoSupplier.Binder());
        bind(this.serviceLocator, new TagDaoSupplier.Binder());
        bind(this.serviceLocator, new UserAuthDaoSupplier.Binder());
        bind(this.serviceLocator, new UserDaoSupplier.Binder());
        bind(this.serviceLocator, new UserEmailDaoSupplier.Binder());
        bind(this.serviceLocator, new UserRoleDaoSupplier.Binder());
        bind(this.serviceLocator, new CustomLoginServiceSupplier.Binder());
    }

    @Nonnull
    public ServiceLocator getServiceLocator() {
        return this.serviceLocator;
    }

    public <T> T get(@Nonnull final Class<T> clazz) {
        return getServiceLocator().getService(clazz);
    }
}
