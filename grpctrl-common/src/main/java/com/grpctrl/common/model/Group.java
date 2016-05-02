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
import java.util.List;
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
 * A representation of a group with a name and possibly some tags describing it, along with some children as members.
 * This class is immutable.
 */
@Immutable
@ThreadSafe
public class Group implements Comparable<Group> {
    @Nullable
    private final Long id;

    @Nullable
    private final Long parentId;

    @Nonnull
    private final String name;

    @Nonnull
    private final ImmutableSet<Tag> tags;

    @Nonnull
    private final ImmutableSet<Group> children;

    private Group(
            @Nullable final Long id, @Nullable final Long parentId, @Nonnull final String name,
            @Nonnull final ImmutableSet<Tag> tags, @Nonnull final ImmutableSet<Group> children) {
        // These values have already been validated via the builder.
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.tags = tags;
        this.children = children;
    }

    /**
     * @return the unique group identifier
     */
    @Nonnull
    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @return whether this group has a unique identifier assigned
     */
    public boolean hasId() {
        return getId().isPresent();
    }

    /**
     * @return the unique identifier of the group in which this group resides, if available
     */
    @Nonnull
    public Optional<Long> getParentId() {
        return Optional.ofNullable(this.parentId);
    }

    /**
     * @return whether this group has a parent id
     */
    public boolean hasParentId() {
        return getParentId().isPresent();
    }

    /**
     * @return the name of this group
     */
    @Nonnull
    public String getName() {
        return this.name;
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

        final OptionalComparator<Long> optionalComparator = new OptionalComparator<>();

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getName(), other.getName());
        cmp.append(getId(), other.getId(), optionalComparator);
        cmp.append(getParentId(), other.getParentId(), optionalComparator);
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
        hash.append(getName());
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
        str.append("name", getName());
        str.append("tags", getTags());
        str.append("children", getChildren());
        return str.build();
    }

    /**
     * Determine whether the specified name matches the name of the current group, or exists within the current group as
     * a name of one of the children.
     *
     * @param name the group name to check for existence within this group
     *
     * @return whether the specified name was found
     */
    public boolean containsName(@Nonnull final String name) {
        if (getName().equals(Objects.requireNonNull(name))) {
            return true;
        }

        for (final Group child : getChildren()) {
            if (child.containsName(name)) {
                return true;
            }
        }

        return false;
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

    /**
     * Used to manage the creation of {@link Group} objects. This class is not thread safe and should not be used across
     * multiple threads.
     */
    @NotThreadSafe
    public static class Builder {
        @Nullable
        private Long id;

        @Nullable
        private Long parentId;

        @Nonnull
        private String name = "";

        @Nonnull
        private final Set<Tag> tags = new LinkedHashSet<>();

        @Nonnull
        private final Set<Group> children = new LinkedHashSet<>();

        @Nonnull
        private final List<Builder> childBuilders = new LinkedList<>();

        /**
         * Initialize a builder with the specified descriptive name.
         *
         * @param name the descriptive name of the group to be created
         *
         * @throws NullPointerException if the provided parameter is {@code null}
         * @throws IllegalArgumentException if the provided parameter is invalid
         */
        public Builder(@Nonnull final String name) {
            setName(name);
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

            setId(other.getId().orElse(null));
            setParentId(other.getParentId().orElse(null));
            setName(other.getName());
            addTags(other.getTags());
            addChildren(other.getChildren());
        }

        /**
         * @param id the new unique id of the group to be created
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder setId(@Nullable final Long id) {
            this.id = id;
            return this;
        }

        /**
         * Removes the id value from this group.
         *
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder clearId() {
            this.id = null;
            return this;
        }

        /**
         * @param parentId the new unique id of the parent group in which this group will reside
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder setParentId(@Nullable final Long parentId) {
            this.parentId = parentId;
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
         * @param name the new descriptive name of the group to be created
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         * @throws IllegalArgumentException if the parameter is invalid
         */
        @Nonnull
        public Builder setName(@Nonnull final String name) {
            this.name = Validator.validateName(name);
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
        public Builder addChild(@Nonnull final Group group) {
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
         * @param builder the new child group builder to include as a member of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChildBuilder(@Nonnull final Builder builder) {
            return addChildBuilders(builder);
        }

        /**
         * @param builders the new child group builders to include as members of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChildBuilders(@Nonnull final Builder... builders) {
            return addChildBuilders(Arrays.asList(Objects.requireNonNull(builders)));
        }

        /**
         * @param builders the new child group builders to include as members of the created group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder addChildBuilders(@Nonnull final Collection<Builder> builders) {
            this.childBuilders.addAll(Objects.requireNonNull(builders));
            return this;
        }

        /**
         * Using the current values in this builder, create and return a new group.
         *
         * @return the group object represented by this builder
         */
        @Nonnull
        public Group build() {
            // Turn any child builders into real children.
            if (!this.childBuilders.isEmpty()) {
                this.childBuilders.stream().map(Builder::build).forEach(this.children::add);
                this.childBuilders.clear();
            }

            return new Group(this.id, this.parentId, this.name, ImmutableSet.copyOf(this.tags),
                    ImmutableSet.copyOf(this.children));
        }
    }
}
