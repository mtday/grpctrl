package com.grpctrl.common.model;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A representation of the service level allowed for an account, specifying things like the maximum number of groups
 * the account can create, the maximum number of tags per group, etc. This class is immutable.
 */
@Immutable
@ThreadSafe
public class ServiceLevel implements Comparable<ServiceLevel> {
    private final int maxGroups;
    private final int maxTags;
    private final int maxDepth;

    private ServiceLevel(final int maxGroups, final int maxTags, final int maxDepth) {
        // These values have already been validated via the builder.
        this.maxGroups = maxGroups;
        this.maxTags = maxTags;
        this.maxDepth = maxDepth;
    }

    /**
     * @return the maximum number of groups the account can possess
     */
    public int getMaxGroups() {
        return this.maxGroups;
    }

    /**
     * @return the maximum number of tags the account can possess, per group
     */
    public int getMaxTags() {
        return this.maxTags;
    }

    /**
     * @return the maximum depth of groups within groups the account supports
     */
    public int getMaxDepth() {
        return this.maxDepth;
    }

    @Override
    public int compareTo(@Nullable final ServiceLevel other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getMaxGroups(), other.getMaxGroups());
        cmp.append(getMaxTags(), other.getMaxTags());
        cmp.append(getMaxDepth(), other.getMaxDepth());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof ServiceLevel && compareTo((ServiceLevel) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getMaxGroups());
        hash.append(getMaxTags());
        hash.append(getMaxDepth());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("maxGroups", getMaxGroups());
        str.append("maxTags", getMaxTags());
        str.append("maxDepth", getMaxDepth());
        return str.build();
    }

    /**
     * Performs validation on service level fields.
     */
    public static class Validator {
        /**
         * Perform validation on the provided {@code maxGroups}.
         *
         * @param maxGroups the maximum number of groups the account is allowed to possess
         *
         * @return the unmodified value, when valid
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        public static int validateMaxGroups(final int maxGroups) {
            Preconditions.checkArgument(maxGroups > 0, "The maximum number of groups must be positive");

            return maxGroups;
        }

        /**
         * Perform validation on the provided {@code maxTags}.
         *
         * @param maxTags the maximum number of tags the account is allowed to possess, per group
         *
         * @return the unmodified value, when valid
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        public static int validateMaxTags(final int maxTags) {
            Preconditions.checkArgument(maxTags > 0, "The maximum number of tags per group must be positive");

            return maxTags;
        }

        /**
         * Perform validation on the provided {@code maxDepth}.
         *
         * @param maxDepth the maximum depth of groups within groups the account supports
         *
         * @return the unmodified value, when valid
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        public static int validateMaxDepth(final int maxDepth) {
            Preconditions.checkArgument(maxDepth > 0, "The maximum depth of groups within groups must be positive");

            return maxDepth;
        }
    }

    /**
     * Responsible for creating service level objects.
     */
    public static class Builder {
        private int maxGroups = 100;
        private int maxTags = 10;
        private int maxDepth = 3;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy the provided service level.
         *
         * @param other the service level to duplicate
         */
        public Builder(@Nonnull final ServiceLevel other) {
            Objects.requireNonNull(other);

            setMaxGroups(other.getMaxGroups());
            setMaxTags(other.getMaxTags());
            setMaxDepth(other.getMaxDepth());
        }

        /**
         * @param maxGroups the new value indicating the maximum number of groups the account can possess
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        @Nonnull
        public Builder setMaxGroups(final int maxGroups) {
            this.maxGroups = Validator.validateMaxGroups(maxGroups);
            return this;
        }

        /**
         * @param maxTags the new value indicating the maximum number of tags the account can possess, per group
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        @Nonnull
        public Builder setMaxTags(final int maxTags) {
            this.maxTags = Validator.validateMaxTags(maxTags);
            return this;
        }

        /**
         * @param maxDepth the new value indicating the maximum depth of groups within groups the account supports
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        @Nonnull
        public Builder setMaxDepth(final int maxDepth) {
            this.maxDepth = Validator.validateMaxDepth(maxDepth);
            return this;
        }

        /**
         * Create the service level object based on the current values.
         *
         * @return the configured service level
         */
        @Nonnull
        public ServiceLevel build() {
            // Make sure all of the values have been configured.
            Validator.validateMaxGroups(this.maxGroups);
            Validator.validateMaxTags(this.maxTags);
            Validator.validateMaxDepth(this.maxDepth);

            return new ServiceLevel(this.maxGroups, this.maxTags, this.maxDepth);
        }
    }
}
