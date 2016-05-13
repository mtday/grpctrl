package com.grpctrl.rest.resource.v1.group;

import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.GroupDao;
import com.grpctrl.db.dao.supplier.GroupDaoSupplier;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The base class for group resources.
 */
public class BaseGroupResource {
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final GroupDaoSupplier groupDaoSupplier;

    /**
     * @param objectMapperSupplier the {@link ObjectMapperSupplier} responsible for generating JSON data
     * @param groupDaoSupplier the {@link GroupDaoSupplier} used to perform the group operation
     */
    @Inject
    public BaseGroupResource(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final GroupDaoSupplier groupDaoSupplier) {
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.groupDaoSupplier = Objects.requireNonNull(groupDaoSupplier);
    }

    /**
     * @return the object mapper responsible for generating JSON data
     */
    @Nonnull
    public ObjectMapperSupplier getObjectMapperSupplier() {
        return this.objectMapperSupplier;
    }

    /**
     * @return the {@link GroupDao} used to perform the group operation
     */
    @Nonnull
    public GroupDaoSupplier getGroupDaoSupplier() {
        return this.groupDaoSupplier;
    }
}
