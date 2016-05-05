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

/**
 * A representation of the service level allowed for an account, specifying things like the maximum number of groups
 * the account can create, the maximum number of tags the account can add, etc.
 */
public class ServiceLevel implements Comparable<ServiceLevel> {
    private int maxGroups = 100;
    private int maxTags = 1000;
    private int maxDepth = 3;

    /**
     * Default constructor.
     */
    public ServiceLevel() {
    }

    /**
     * @param maxGroups the maximum number of groups the account can possess
     * @param maxTags the maximum number of tags the account can possess
     * @param maxDepth the maximum depth of groups within groups the account supports
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public ServiceLevel(final int maxGroups, final int maxTags, final int maxDepth) {
        setMaxGroups(maxGroups);
        setMaxTags(maxTags);
        setMaxDepth(maxDepth);
    }

    /**
     * @param other the service level to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public ServiceLevel(@Nonnull final ServiceLevel other) {
        setValues(other);
    }

    /**
     * @param other the service level to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public ServiceLevel setValues(@Nonnull final ServiceLevel other) {
        Objects.requireNonNull(other);
        setMaxGroups(other.getMaxGroups());
        setMaxTags(other.getMaxTags());
        setMaxDepth(other.getMaxDepth());
        return this;
    }

    /**
     * @return the maximum number of groups the account can possess
     */
    public int getMaxGroups() {
        return this.maxGroups;
    }

    /**
     * @param maxGroups the new maximum number of groups the account can possess
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the provided parameter is invalid
     */
    public ServiceLevel setMaxGroups(final int maxGroups) {
        this.maxGroups = Validator.validateMaxGroups(maxGroups);
        return this;
    }

    /**
     * @return the maximum number of tags the account can possess
     */
    public int getMaxTags() {
        return this.maxTags;
    }

    /**
     * @param maxTags the new maximum number of tags the account can possess
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the provided parameter is invalid
     */
    public ServiceLevel setMaxTags(final int maxTags) {
        this.maxTags = Validator.validateMaxTags(maxTags);
        return this;
    }

    /**
     * @return the maximum depth of groups within groups the account supports
     */
    public int getMaxDepth() {
        return this.maxDepth;
    }

    /**
     * @param maxDepth the new maximum number of groups within groups the account supports
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the provided parameter is invalid
     */
    public ServiceLevel setMaxDepth(final int maxDepth) {
        this.maxDepth = Validator.validateMaxDepth(maxDepth);
        return this;
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
         * @param maxTags the maximum number of tags the account is allowed to possess
         *
         * @return the unmodified value, when valid
         *
         * @throws IllegalArgumentException if the provided value is invalid
         */
        public static int validateMaxTags(final int maxTags) {
            Preconditions.checkArgument(maxTags > 0, "The maximum number of tags must be positive");

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
}
