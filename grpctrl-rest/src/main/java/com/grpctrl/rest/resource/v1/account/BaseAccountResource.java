package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.db.dao.AccountDao;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The base class for account resources.
 */
public class BaseAccountResource {
    @Nonnull
    private final ObjectMapper objectMapper;
    @Nonnull
    private final AccountDao accountDao;

    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public BaseAccountResource(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.accountDao = Objects.requireNonNull(accountDao);
    }

    /**
     * @return the object mapper responsible for generating JSON data
     */
    @Nonnull
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * @return the {@link AccountDao} used to perform the account operation
     */
    @Nonnull
    public AccountDao getAccountDao() {
        return this.accountDao;
    }
}
