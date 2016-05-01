package com.grpctrl.common.model;

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
 * A representation of a service account in this system. This class is immutable.
 */
@Immutable
@ThreadSafe
public class Account implements Comparable<Account> {
    @Nonnull
    private final String id;

    @Nonnull
    private final ServiceLevel serviceLevel;

    /**
     * Represents a unique service account.
     *
     * @param id the unique account identifier
     * @param serviceLevel the level of service made available for this account
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Account(@Nonnull final String id, @Nonnull final ServiceLevel serviceLevel) {
        this.id = Validator.validateId(id);
        this.serviceLevel = Objects.requireNonNull(serviceLevel);
    }

    /**
     * @return the unique account identifier
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the level of service made available for this account
     */
    @Nonnull
    public ServiceLevel getServiceLevel() {
        return this.serviceLevel;
    }

    @Override
    public int compareTo(@Nullable final Account other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId());
        cmp.append(getServiceLevel(), other.getServiceLevel());
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
        hash.append(getServiceLevel());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("serviceLevel", getServiceLevel());
        return str.build();
    }

    /**
     * Performs validation on account fields.
     */
    public static class Validator {
        /** The maximum size of the account id. */
        public static final int MAX_ID_LENGTH = 30;

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

    /**
     * Responsible for building account objects.
     */
    public static class Builder {
        @Nonnull
        private String id = "";

        @Nonnull
        private ServiceLevel serviceLevel =
                new ServiceLevel.Builder().setMaxGroups(100).setMaxTags(10).setMaxChildren(10).setMaxDepth(3).build();

        /**
         * Initialize a builder with the unique account identifier and service level.
         *
         * @param id the unique id of the account to be created
         * @param serviceLevel the level of service made available for this account
         *
         * @throws NullPointerException if either of the provided parameters is {@code null}
         * @throws IllegalArgumentException if either of the provided parameters is invalid
         */
        public Builder(@Nonnull final String id, @Nonnull final ServiceLevel serviceLevel) {
            setId(id);
            setServiceLevel(serviceLevel);
        }

        /**
         * Initialize a builder using the provided account values.
         *
         * @param other the account to duplicate
         *
         * @throws NullPointerException if the provided parameter is {@code null}
         */
        public Builder(@Nonnull final Account other) {
            this(Objects.requireNonNull(other).getId(), other.getServiceLevel());
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
         * @param serviceLevel the new unique id of the group to be created
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        @Nonnull
        public Builder setServiceLevel(@Nonnull final ServiceLevel serviceLevel) {
            this.serviceLevel = Objects.requireNonNull(serviceLevel);
            return this;
        }

        /**
         * Create an account based on the current state of this builder.
         *
         * @return the account represented by this builder
         */
        @Nonnull
        public Account build() {
            return new Account(this.id, this.serviceLevel);
        }
    }
}
