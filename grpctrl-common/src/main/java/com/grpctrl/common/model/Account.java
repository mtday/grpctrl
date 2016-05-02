package com.grpctrl.common.model;

import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;
import java.util.Optional;

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
    @Nullable
    private final Long id;

    @Nonnull
    private final String name;

    @Nonnull
    private final ServiceLevel serviceLevel;

    private Account(@Nullable final Long id, @Nonnull final String name, @Nonnull final ServiceLevel serviceLevel) {
        // These have already been validated by the builder.
        this.id = id;
        this.name = name;
        this.serviceLevel = serviceLevel;
    }

    /**
     * @return the unique account identifier, if available
     */
    @Nonnull
    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @return whether this account object has a unique identifier
     */
    public boolean hasId() {
        return getId().isPresent();
    }

    /**
     * @return the descriptive name assigned to this account
     */
    @Nonnull
    public String getName() {
        return this.name;
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
        cmp.append(getId(), other.getId(), new OptionalComparator<>());
        cmp.append(getName(), other.getName());
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
        hash.append(getName());
        hash.append(getServiceLevel());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("name", getName());
        str.append("serviceLevel", getServiceLevel());
        return str.build();
    }

    /**
     * Performs validation on account fields.
     */
    public static class Validator {
        /** The maximum size of the account name. */
        public static final int MAX_NAME_LENGTH = 200;

        /**
         * Perform validation on the provided {@code name}.
         *
         * @param name the account name to validate
         *
         * @return the unmodified name, when valid
         *
         * @throws NullPointerException if the provided name is {@code null}
         * @throws IllegalArgumentException if the provided name is invalid
         */
        public static String validateName(@Nonnull final String name) {
            Objects.requireNonNull(name);

            if (name.length() > MAX_NAME_LENGTH) {
                throw new IllegalArgumentException("Invalid account name, it exceeds the max length: " + MAX_NAME_LENGTH);
            }

            return name;
        }
    }

    /**
     * Responsible for building account objects.
     */
    public static class Builder {
        @Nullable
        private Long id;

        @Nonnull
        private String name = "";

        @Nonnull
        private ServiceLevel serviceLevel = new ServiceLevel.Builder().build();

        /**
         * Initialize a builder with the account name and service level.
         *
         * @param name the descriptive name of the account to be created
         * @param serviceLevel the level of service made available for this account
         *
         * @throws NullPointerException if either of the provided parameters is {@code null}
         * @throws IllegalArgumentException if either of the provided parameters is invalid
         */
        public Builder(@Nonnull final String name, @Nonnull final ServiceLevel serviceLevel) {
            this(null, name, serviceLevel);
        }

        /**
         * Initialize a builder with the unique account identifier, account name and service level.
         *
         * @param id the unique account identifier, possibly {@code null}
         * @param name the descriptive name of the account to be created
         * @param serviceLevel the level of service made available for this account
         *
         * @throws NullPointerException if either of the provided parameters is {@code null}
         * @throws IllegalArgumentException if either of the provided parameters is invalid
         */
        public Builder(@Nullable final Long id, @Nonnull final String name, @Nonnull final ServiceLevel serviceLevel) {
            setId(id);
            setName(name);
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
            this(Objects.requireNonNull(other).getId().orElse(null), other.getName(), other.getServiceLevel());
        }

        /**
         * @param id the new unique identifier of the account to be created, possibly {@code null}
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if the parameter is {@code null}
         * @throws IllegalArgumentException if the parameter is invalid
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
         * @param name the new descriptive name of the account to be created
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
            return new Account(this.id, this.name, this.serviceLevel);
        }
    }
}
