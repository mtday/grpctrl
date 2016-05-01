package com.grpctrl.common.model;

import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used to represent the unique id of a group's parent, possibly containing a {@code null} id if the group has no
 * parent.
 */
public class ParentId implements Comparable<ParentId> {
    @Nullable
    private final String id;

    /**
     * Default constructor means there is no parent id.
     */
    public ParentId() {
        this.id = null;
    }

    /**
     * @param id the parent id if not {@code null}, otherwise no parent
     */
    public ParentId(@Nullable final String id) {
        this.id = id;
    }

    /**
     * @return the unique identifier of the parent group, not present if there is no parent group
     */
    @Nonnull
    public Optional<String> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @return whether a parent id is available
     */
    public boolean hasId() {
        return getId().isPresent();
    }

    @Override
    public int compareTo(@Nullable final ParentId other) {
        if (other == null) {
            return 1;
        }

        return new OptionalComparator<String>().compare(getId(), other.getId());
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof ParentId && compareTo((ParentId) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        return str.build();
    }
}
