package com.grpctrl.common.model;

import com.grpctrl.common.util.CollectionComparator;
import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A representation of a group with a name and possibly some tags describing it.
 */
public class Group implements Comparable<Group> {
    @Nullable
    private Long id;
    @Nullable
    private Long parentId;

    @Nonnull
    private String name = "group-name";
    @Nonnull
    private Set<Tag> tags = new LinkedHashSet<>();

    /**
     * Default constructor.
     */
    public Group() {
    }

    /**
     * @param name the name of this group
     *
     * @throws IllegalArgumentException if the parameter is invalid
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Group(@Nonnull final String name) {
        this(null, name);
    }

    /**
     * @param id the unique group identifier, possibly {@code null}
     * @param name the name of this group
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if the {@code name} parameter is {@code null}
     */
    public Group(@Nullable final Long id, @Nonnull final String name) {
        this(id, null, name);
    }

    /**
     * @param id the unique group identifier, possibly {@code null}
     * @param parentId the unique identifier of the group in which this group resides, possibly {@code null}
     * @param name the name of this group
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if the {@code name} parameter is {@code null}
     */
    public Group(@Nullable final Long id, @Nullable final Long parentId, @Nonnull final String name) {
        setId(id);
        setParentId(parentId);
        setName(name);
    }

    /**
     * @param id the unique group identifier, possibly {@code null}
     * @param parentId the unique identifier of the group in which this group resides, possibly {@code null}
     * @param name the name of this group
     * @param tags the tags assigned to this group
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if the {@code name} or {@code tags} parameters are {@code null}
     */
    public Group(
            @Nullable final Long id, @Nullable final Long parentId, @Nonnull final String name,
            @Nonnull final Set<Tag> tags) {
        setId(id);
        setParentId(parentId);
        setName(name);
        setTags(tags);
    }

    /**
     * @param other the group to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Group(@Nonnull final Group other) {
        setValues(other);
    }

    /**
     * @param other the group to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Group setValues(@Nonnull final Group other) {
        Objects.requireNonNull(other);
        setId(other.getId().orElse(null));
        setParentId(other.getParentId().orElse(null));
        setName(other.getName());
        setTags(other.getTags());
        return this;
    }

    /**
     * @return the unique group identifier
     */
    @Nonnull
    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @param id the new unique group identifier, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public Group setId(@Nullable final Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return the unique identifier of the group in which this group resides, if available
     */
    @Nonnull
    public Optional<Long> getParentId() {
        return Optional.ofNullable(this.parentId);
    }

    /**
     * @param parentId the new unique identifier of the group in which this group resides, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public Group setParentId(@Nullable final Long parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * @return the name of this group
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * @param name the new name of this group
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the provided name is invalid
     * @throws NullPointerException if the provided name is {@code null}
     */
    public Group setName(@Nonnull final String name) {
        this.name = Validator.validateName(name);
        return this;
    }

    /**
     * @return the set of tags assigned to this group, not a copy
     */
    @Nonnull
    public Set<Tag> getTags() {
        return this.tags;
    }

    /**
     * @param tags the new set of tags assigned to this group
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public Group setTags(@Nonnull final Set<Tag> tags) {
        this.tags = Objects.requireNonNull(tags);
        return this;
    }

    /**
     * @param tags the new set of tags assigned to this group
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public Group setTags(@Nonnull final Tag... tags) {
        this.tags = new LinkedHashSet<>(Arrays.asList(Objects.requireNonNull(tags)));
        return this;
    }

    /**
     * @param tags the new tags to be included in this group
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public Group addTags(@Nonnull final Set<Tag> tags) {
        this.tags.addAll(Objects.requireNonNull(tags));
        return this;
    }

    /**
     * @param tags the new tags to be included in this group
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public Group addTags(@Nonnull final Tag... tags) {
        this.tags.addAll(Arrays.asList(Objects.requireNonNull(tags)));
        return this;
    }

    @Override
    public int compareTo(@Nullable final Group other) {
        if (other == null) {
            return 1;
        }

        final OptionalComparator<Long> optionalComparator = new OptionalComparator<>();

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getName(), other.getName());
        cmp.append(getId(), other.getId(), optionalComparator);
        cmp.append(getParentId(), other.getParentId(), optionalComparator);
        cmp.append(getTags(), other.getTags(), new CollectionComparator<Tag>());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof Group && compareTo((Group) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        hash.append(getParentId());
        hash.append(getName());
        hash.append(getTags());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("parentId", getParentId());
        str.append("name", getName());
        str.append("tags", getTags());
        return str.build();
    }

    /**
     * Performs validation on group fields.
     */
    public static class Validator {
        /** The maximum size of the group name. */
        public static final int MAX_NAME_LENGTH = 200;

        /**
         * Perform validation on the provided {@code name}.
         *
         * @param name the group name to validate
         *
         * @return the unmodified name, when valid
         *
         * @throws NullPointerException if the provided name is {@code null}
         * @throws IllegalArgumentException if the provided name is invalid
         */
        public static String validateName(@Nonnull final String name) {
            Objects.requireNonNull(name);

            if (name.length() > MAX_NAME_LENGTH) {
                throw new IllegalArgumentException("Invalid name, it exceeds the max length: " + MAX_NAME_LENGTH);
            }

            return name;
        }
    }
}
