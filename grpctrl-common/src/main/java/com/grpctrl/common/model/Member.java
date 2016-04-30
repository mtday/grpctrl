package com.grpctrl.common.model;

import com.google.common.collect.ImmutableSet;
import com.grpctrl.common.util.CollectionComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A representation of a member in a group, where a member has a unique id and possibly some tags describing it. This
 * class is immutable.
 */
public class Member implements Comparable<Member> {
    @Nonnull
    private final String id;

    @Nonnull
    private final ImmutableSet<Tag> tags;

    /**
     * Represents a member of a group, with the member having a unique id.
     *
     * @param id the unique member identifier
     *
     * @throws NullPointerException if the provided id is {@code null}
     * @throws IllegalArgumentException if the provided id is invalid
     */
    public Member(@Nonnull final String id) {
        this(id, Collections.emptyList());
    }

    /**
     * Represents a member of a group, with the member having a unique id.
     *
     * @param id the unique member identifier
     * @param tags the collection of tags associated with the member
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Member(@Nonnull final String id, @Nonnull final Collection<Tag> tags) {
        this.id = Validator.validateId(id);
        this.tags = ImmutableSet.copyOf(tags);
    }

    /**
     * @return the member type representing whether this member is a specific individual, or a group of individuals
     */
    @Nonnull
    public MemberType getMemberType() {
        return MemberType.INDIVIDUAL;
    }

    /**
     * @return the unique group identifier
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the immutable set of tags assigned to this group
     */
    @Nonnull
    public ImmutableSet<Tag> getTags() {
        return this.tags;
    }

    @Override
    public int compareTo(@Nullable final Member other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getMemberType(), other.getMemberType());
        cmp.append(getId(), other.getId());
        cmp.append(getTags(), other.getTags(), new CollectionComparator<Tag>());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof Member && compareTo((Member) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getMemberType().name()); // Name is used here to make the hashes deterministic and consistent.
        hash.append(getId());
        hash.append(getTags());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("memberType", getMemberType());
        str.append("id", getId());
        str.append("tags", getTags());
        return str.build();
    }

    /**
     * Performs validation on member fields.
     */
    public static class Validator {
        /** The maximum size of the member id. */
        public static final int MAX_ID_LENGTH = 200;

        /**
         * Perform validation on the provided {@code id}.
         *
         * @param id the member id to validate
         *
         * @return the unmodified id, when valid
         *
         * @throws NullPointerException if the provided id is {@code null}
         * @throws IllegalArgumentException if the provided id is invalid
         */
        public static String validateId(@Nonnull final String id) {
            Objects.requireNonNull(id);

            if (id.length() > MAX_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid member id, it exceeds the max length: " + MAX_ID_LENGTH);
            }

            return id;
        }
    }
}
