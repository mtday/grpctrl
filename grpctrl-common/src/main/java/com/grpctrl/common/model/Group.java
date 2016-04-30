package com.grpctrl.common.model;

import com.google.common.collect.ImmutableSet;
import com.grpctrl.common.util.CollectionComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A representation of a group with a unique id and possibly some tags describing it, along with some members. Note that
 * a group can also act as a member, providing for the capability of groups containing groups. This class is immutable.
 */
public class Group extends Member {
    @Nonnull
    private final ImmutableSet<Member> members;

    /**
     * Represents a group with no tags or members.
     *
     * @param id the unique group identifier
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Group(@Nonnull final String id) {
        this(id, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Represents a group with tags describing it, but no members.
     *
     * @param id the unique member identifier
     * @param tags the collection of tags associated with the group
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Group(@Nonnull final String id, @Nonnull final Collection<Tag> tags) {
        this(id, tags, Collections.emptyList());
    }

    /**
     * Represents a group with tags describing it, along with members within it.
     *
     * @param id the unique member identifier
     * @param tags the collection of tags associated with the group
     * @param members the collection of members within the group
     *
     * @throws NullPointerException if any of the provided parameters are {@code null}
     * @throws IllegalArgumentException if any of the provided parameters are invalid
     */
    public Group(
            @Nonnull final String id, @Nonnull final Collection<Tag> tags, @Nonnull final Collection<Member> members) {
        super(id, tags);
        this.members = ImmutableSet.copyOf(members);
    }

    /**
     * @return the member type representing whether this member is a specific individual, or a group of individuals
     */
    @Override
    @Nonnull
    public MemberType getMemberType() {
        return MemberType.GROUP;
    }

    /**
     * @return the immutable set of members assigned to this group
     */
    @Nonnull
    public ImmutableSet<Member> getMembers() {
        return this.members;
    }

    @Override
    public int compareTo(@Nullable final Member other) {
        if (other == null) {
            return 1;
        }

        // Since Group extends Member, we need to be able to compare groups with members.

        if (other instanceof Group) {
            final Group group = (Group) other;
            final CompareToBuilder cmp = new CompareToBuilder();
            cmp.append(getMemberType(), group.getMemberType());
            cmp.append(getId(), group.getId());
            cmp.append(getTags(), group.getTags(), new CollectionComparator<Tag>());
            cmp.append(getMembers(), group.getMembers(), new CollectionComparator<Member>());
            return cmp.toComparison();
        }

        return super.compareTo(other);
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof Member && compareTo((Member) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(super.hashCode());
        hash.append(getMembers());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("memberType", getMemberType());
        str.append("id", getId());
        str.append("tags", getTags());
        str.append("members", getMembers());
        return str.build();
    }
}
