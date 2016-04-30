package com.grpctrl.service;

import static com.grpctrl.common.model.ServiceType.DATABASE;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.config.ConfigSupplier;
import com.grpctrl.common.model.ServiceType;
import com.grpctrl.service.impl.DatabaseGroupControlService;
import com.grpctrl.service.impl.MemoryGroupControlService;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides singleton access to the {@link GroupControlService} implementation.
 */
public class GroupControlServiceSupplier implements Supplier<GroupControlService>, Factory<GroupControlService> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nullable
    private volatile GroupControlService singleton;

    /**
     * Create the supplier with the necessary dependencies.
     *
     * @param configSupplier the {@link ConfigSupplier} responsible for providing access to the static system
     * configuration
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    @Inject
    public GroupControlServiceSupplier(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    private ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public GroupControlService get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (GroupControlServiceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public GroupControlService provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final GroupControlService service) {
        // No need to do anything here.
    }

    @Nonnull
    private GroupControlService create() {
        final ServiceType serviceType =
                ServiceType.valueOf(getConfigSupplier().get().getString(ConfigKeys.SERVICE_IMPL.getKey()));

        if (serviceType == DATABASE) {
            return new DatabaseGroupControlService();
        }

        return new MemoryGroupControlService();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(GroupControlServiceSupplier.class).to(GroupControlServiceSupplier.class).in(Singleton.class);
            bindFactory(GroupControlServiceSupplier.class).to(GroupControlService.class).in(Singleton.class);
        }
    }
}
