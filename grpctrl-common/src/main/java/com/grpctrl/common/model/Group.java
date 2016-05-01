package com.grpctrl.common.model;

import com.google.common.collect.ImmutableSet;
import com.grpctrl.common.util.CollectionComparator;
import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A representation of a group with a unique id and possibly some tags describing it, along with some members. This
 * class is immutable.
 */
@Immutable
@ThreadSafe
public class Group implements Comparable<Group> {
    @Nonnull
    private final String id;

    @Nullable
    private final String parentId;

    @Nonnull
    private final ImmutableSet<Tag> tags;

    @Nonnull
    private final ImmutableSet<Group> children;

    private Group(
            @Nonnull final String id, @Nullable final String parentId, @Nonnull final ImmutableSet<Tag> tags,
            @Nonnull final ImmutableSet<Group> children) {
        // These values have already been validated via the builder.
        this.id = id;
        this.parentId = parentId;
        this.tags = tags;
        this.children = children;
    }

    /**
     * @return the unique group identifier
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the unique identifier of the group in which this group resides, if available
     */
    @Nonnull
    public Optional<String> getParentId() {
        return Optional.ofNullable(this.parentId);
    }

    /**
     * @return whether this group has a parent id
     */
    public boolean hasParentId() {
        return getParentId().isPresent();
    }

    /**
     * @return the immutable set of tags assigned to this group
     */
    @Nonnull
    public ImmutableSet<Tag> getTags() {
        return this.tags;
    }

    /**
     * @return whether this group has any assigned tags
     */
    public boolean hasTags() {
        return !getTags().isEmpty();
    }

    /**
     * @return the immutable set of children contained within this group
     */
    @Nonnull
    public ImmutableSet<Group> getChildren() {
        return this.children;
    }

    /**
     * @return whether this group has any assigned children
     */
    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @Override
    public int compareTo(@Nullable final Group other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId());
        cmp.append(getParentId(), other.getParentId(), new OptionalComparator<String>());
        cmp.append(getTags(), other.getTags(), new CollectionComparator<Tag>());
        cmp.append(getChildren(), other.getChildren(), new CollectionComparator<Group>());
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
        hash.append(getTags());
        hash.append(getChildren());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("parentId", getParentId());
        str.append("tags", getTags());
        str.append("children", getChildren());
        return str.build();
    }

    /**
     * Determine whether the specified id matches the id of the current group, or exists within the current group as
     * one of the children.
     *
     * @param id the unique group identifier to check for existence within this group
     *
     * @return whether the specified id was found
     */
    public boolean contains(@Nonnull final String id) {
        if (getId().equals(id)) {
            return true;
        }

        for (final Group child : getChildren()) {
            if (child.contains(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Flatten the provided collection of hierarchical groups into a flat list of groups added depth-first.
     *
     * @param groups the collection of groups to flatten
     *
     * @return a flattened collection of the provided groups
     */
    public static Collection<Group> flatten(@Nonnull final Collection<Group> groups) {
        return flatten(null, groups);
    }

    /**
     * Flatten the provided collection of hierarchical groups into a flat list of groups added depth-first, with the
     * top-level groups falling into the specified parent group.
     *
     * @param parentId the parent id to assign to the top-level groups in the provided collection
     * @param groups the collection of groups to flatten
     *
     * @return a flattened collection of the provided groups
     */
    public static Collection<Group> flatten(@Nullable final String parentId, @Nonnull final Collection<Group> groups) {
        final Collection<Group> collection = new LinkedList<>();
        flatten(collection, parentId, Objects.requireNonNull(groups));
        return collection;
    }

    private static void flatten(
            @Nonnull final Collection<Group> collection, @Nullable final String parentId,
            @Nonnull final Collection<Group> groups) {
        for (final Group group : groups) {
            collection.add(new Group.Builder(group).setParentId(parentId).clearChildren().build());
            flatten(collection, group.getId(), group.getChildren());
        }
    }

    /**
     * Performs validation on group fields.
     */
    public static class Validator {
        /** The maximum size of the group id. */
        public static final int MAX_ID_LENGTH = 200;

        /**
         * Perform validation on the provided {@code id}.
         *
         * @param id the group id to validate
         *
         * @return the unmodified id, when valid
         *
         * @throws NullPointerException if the provided id is {@code null}
         * @throws IllegalArgumentException if the provided id is invalid
         */
        public static String validateId(@Nonnull final String id) {
            Objects.requireNonNull(id);

            if (id.length() > MAX_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid id, it exceeds the max length: " + MAX_ID_LENGTH);
            }

            return id;
        }

        /**
         * Perform validation on the provided {@code parentId}.
         *
         * @param parentId the group parent id to validate, possibly {@code null}
         *
         * @return the unmodified parent id, when valid
         *
         * @throws IllegalArgumentException if the provided parent id is invalid
         */
        public static String validateParentId(@Nullable final String parentId) {
            if (parentId != null && parentId.length() > MAX_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid parent id, it exceeds the max length: " + MAX_ID_LENGTH);
            }

            return parentId;
        }
    }

    /**
     * Used to manage the creation of {@link Group} objects. This class is not thread safe and should not be used across
     * multiple threads.
     */
    @NotThreadSafe
    public static class Builder {
        @Nonnull
        private String id = "";

        @Nullable
        private String parentId;

        @Nonnull
        private final Set<Tag> tags = new LinkedHashSet<>();

        @Nonnull
        private final Set<Group> children = new LinkedHashSet<>();

        /**
         * Initialize a builder with the unique group identifier.
         *
         * @param id the unique id of the group to be created
         *
         * @throws NullPointerException if the provided parameter is {@code null}
         * @throws IllegalArgumentException if the provided parameter is invalid
         */
        public Builder(@Nonnull final String id) {
            setId(id);
        }

        /**
         * Initialize a builder based on an existing group (copy its contents).
         *
         * @param other the existing group to duplicate
         *
         * @throws NullPointerException if the provided parameter is {@code null}
         */
        public Builder(@Nonnull final Group other) {
            Objects.requireNonNull(other);

            setId(other.getId());
            setParentId(other.getParentId().orElse(null));
            addTags(other.getTags());
            addChildren(other.getChildren());
        }

        /**
         * @param id the new unique id of the group to be created
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         * @throws IllegalArgumentException if the parameter is invalid
         */
        @Nonnull
        public Builder setId(@Nonnull final String id) {
            this.id = Validator.validateId(id);
            return this;
        }

        /**
         * @param parentId the new unique id of the parent group in which this group will reside
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         * @throws IllegalArgumentException if the parameter is invalid
         */
        @Nonnull
        public Builder setParentId(@Nullable final String parentId) {
            this.parentId = Validator.validateParentId(parentId);
            return this;
        }

        /**
         * Removes the parent id value from this group.
         *
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder clearParentId() {
            this.parentId = null;
            return this;
        }

        /**
         * @param tag the new tag to include in the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addTag(@Nonnull final Tag tag) {
            return addTags(tag);
        }

        /**
         * @param tags the new tags to include in the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addTags(@Nonnull final Tag... tags) {
            return addTags(Arrays.asList(Objects.requireNonNull(tags)));
        }

        /**
         * @param tags the new tags to include in the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addTags(@Nonnull final Collection<Tag> tags) {
            this.tags.addAll(Objects.requireNonNull(tags));
            return this;
        }

        /**
         * @param tag the tag to remove from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeTag(@Nonnull final Tag tag) {
            return removeTags(tag);
        }

        /**
         * @param tags the tags to remove from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeTags(@Nonnull final Tag... tags) {
            return removeTags(Arrays.asList(Objects.requireNonNull(tags)));
        }

        /**
         * @param tags the tags to remove from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeTags(@Nonnull final Collection<Tag> tags) {
            this.tags.removeAll(Objects.requireNonNull(tags));
            return this;
        }

        /**
         * Removes all tags from this group.
         *
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder clearTags() {
            this.tags.clear();
            return this;
        }

        /**
         * @param group the new child group to include as a member of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChild(@Nullable final Group group) {
            return addChildren(group);
        }

        /**
         * @param children the new children groups to include as members of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChildren(@Nonnull final Group... children) {
            return addChildren(Arrays.asList(Objects.requireNonNull(children)));
        }

        /**
         * @param children the new children groups to include as members of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChildren(@Nonnull final Collection<Group> children) {
            this.children.addAll(Objects.requireNonNull(children));
            return this;
        }

        /**
         * @param group the child group to be removed as a member from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeChild(@Nonnull final Group group) {
            return removeChildren(group);
        }

        /**
         * @param children the children groups to be removed as a member from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeChildren(@Nonnull final Group... children) {
            return removeChildren(Arrays.asList(Objects.requireNonNull(children)));
        }

        /**
         * @param children the children groups to be removed as a member from this group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder removeChildren(@Nonnull final Collection<Group> children) {
            this.children.removeAll(Objects.requireNonNull(children));
            return this;
        }

        /**
         * Removes all children as members from this group.
         *
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder clearChildren() {
            this.children.clear();
            return this;
        }

        /**
         * Using the current values in this builder, create and return a new group.
         *
         * @return the group object represented by this builder
         */
        @Nonnull
        public Group build() {
            return new Group(
                    this.id, this.parentId, ImmutableSet.copyOf(this.tags), ImmutableSet.copyOf(this.children));
        }
    }
}
