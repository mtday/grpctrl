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
 * A representation of a user account in this system.
 */
public class Account implements Comparable<Account> {
    @Nonnull
    private final String id;

    @Nonnull
    private final ImmutableSet<Tag> tags;

    /**
     * Represents a unique user account.
     *
     * @param id the unique account identifier
     *
     * @throws NullPointerException if the provided id is {@code null}
     * @throws IllegalArgumentException if the provided id is invalid
     */
    public Account(@Nonnull final String id) {
        this(id, Collections.emptyList());
    }

    /**
     * Represents a unique user account, with the account having a unique id and associated tags.
     *
     * @param id the unique user account identifier
     * @param tags the collection of tags associated with the user account
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Account(@Nonnull final String id, @Nonnull final Collection<Tag> tags) {
        this.id = Validator.validateId(id);
        this.tags = ImmutableSet.copyOf(tags);
    }

    /**
     * @return the unique account identifier
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the immutable set of tags assigned to this account
     */
    @Nonnull
    public ImmutableSet<Tag> getTags() {
        return this.tags;
    }

    @Override
    public int compareTo(@Nullable final Account other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId());
        cmp.append(getTags(), other.getTags(), new CollectionComparator<Tag>());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof Account && compareTo((Account) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        hash.append(getTags());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("tags", getTags());
        return str.build();
    }

    /**
     * Performs validation on account fields.
     */
    public static class Validator {
        /** The maximum size of the account id. */
        public static final int MAX_ID_LENGTH = 200;

        /**
         * Perform validation on the provided {@code id}.
         *
         * @param id the account id to validate
         *
         * @return the unmodified id, when valid
         *
         * @throws NullPointerException if the provided id is {@code null}
         * @throws IllegalArgumentException if the provided id is invalid
         */
        public static String validateId(@Nonnull final String id) {
            Objects.requireNonNull(id);

            if (id.length() > MAX_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid account id, it exceeds the max length: " + MAX_ID_LENGTH);
            }

            return id;
        }
    }
}
