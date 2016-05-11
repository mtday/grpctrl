package com.grpctrl.rest.resource.v1.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.db.dao.GroupDao;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The base class for group resources.
 */
public class BaseGroupResource {
    @Nonnull
    private final ObjectMapper objectMapper;
    @Nonnull
    private final GroupDao groupDao;

    /**
     * @param objectMapper the {@link ObjectMapper} responsible for generating JSON data
     * @param groupDao the {@link GroupDao} used to perform the group operation
     */
    @Inject
    public BaseGroupResource(@Nonnull final ObjectMapper objectMapper, @Nonnull final GroupDao groupDao) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.groupDao = Objects.requireNonNull(groupDao);
    }

    /**
     * @return the object mapper responsible for generating JSON data
     */
    @Nonnull
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * @return the {@link GroupDao} used to perform the group operation
     */
    @Nonnull
    public GroupDao getGroupDao() {
        return this.groupDao;
    }
}
