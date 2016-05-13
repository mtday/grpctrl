package com.grpctrl.rest.resource.v1.account;

import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;
import com.grpctrl.rest.resource.v1.BaseResource;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The base class for account resources.
 */
public class BaseAccountResource extends BaseResource {
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final AccountDaoSupplier accountDaoSupplier;

    /**
     * @param objectMapperSupplier the {@link ObjectMapperSupplier} responsible for generating JSON data
     * @param accountDaoSupplier the {@link AccountDaoSupplier} used to perform the account operation
     */
    @Inject
    public BaseAccountResource(@Nonnull final ObjectMapperSupplier objectMapperSupplier, @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.accountDaoSupplier = Objects.requireNonNull(accountDaoSupplier);
    }

    /**
     * @return the {@link ObjectMapperSupplier} responsible for generating JSON data
     */
    @Nonnull
    public ObjectMapperSupplier getObjectMapperSupplier() {
        return this.objectMapperSupplier;
    }

    /**
     * @return the {@link AccountDaoSupplier} used to perform the account operation
     */
    @Nonnull
    public AccountDaoSupplier getAccountDaoSupplier() {
        return this.accountDaoSupplier;
    }
}
