package com.grpctrl.common.model;

import static java.util.Objects.requireNonNull;

import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A representation of a service account in this system.
 */
public class Account implements Comparable<Account> {
    @Nullable
    private Long id;

    @Nonnull
    private String name = "";

    @Nonnull
    private ServiceLevel serviceLevel = new ServiceLevel();

    /**
     * Default constructor.
     */
    public Account() {
    }

    /**
     * @param name the descriptive name assigned to this account
     *
     * @throws IllegalArgumentException if the name parameter is invalid
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Account(@Nonnull final String name) {
        setName(name);
    }

    /**
     * @param name the descriptive name assigned to this account
     * @param serviceLevel the level of service provided to this account
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if any of the parameters are {@code null}
     */
    public Account(@Nonnull final String name, @Nonnull final ServiceLevel serviceLevel) {
        this(null, name, serviceLevel);
    }

    /**
     * @param id the unique account identifier
     * @param name the descriptive name assigned to this account
     * @param serviceLevel the level of service provided to this account
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if the {@code name} or {@code serviceLevel} parameters are {@code null}
     */
    public Account(@Nullable final Long id, @Nonnull final String name, @Nonnull final ServiceLevel serviceLevel) {
        setId(id);
        setName(name);
        setServiceLevel(serviceLevel);
    }

    /**
     * @param other the account to duplicate
     */
    public Account(@Nonnull final Account other) {
        setValues(other);
    }

    /**
     * @param other the account to duplicate
     */
    public Account setValues(@Nonnull final Account other) {
        requireNonNull(other);
        setId(other.getId().orElse(null));
        setName(other.getName());
        setServiceLevel(new ServiceLevel(other.getServiceLevel()));
        return this;
    }

    /**
     * @return the unique account identifier, if available
     */
    @Nonnull
    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @param id the new unique account identifier, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public Account setId(@Nullable final Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return the descriptive name assigned to this account
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * @param name the new descriptive name assigned to this account
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided name is {@code null}
     * @throws IllegalArgumentException if the provided name is invalid
     */
    public Account setName(@Nonnull final String name) {
        this.name = Validator.validateName(name);
        return this;
    }

    /**
     * @return the level of service made available for this account
     */
    @Nonnull
    public ServiceLevel getServiceLevel() {
        return this.serviceLevel;
    }

    /**
     * @param serviceLevel the new level of service provided to this account
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided service level is {@code null}
     */
    public Account setServiceLevel(@Nonnull final ServiceLevel serviceLevel) {
        this.serviceLevel = requireNonNull(serviceLevel);
        return this;
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
            requireNonNull(name);

            if (name.length() > MAX_NAME_LENGTH) {
                throw new IllegalArgumentException("Invalid account name, it exceeds the max length: " + MAX_NAME_LENGTH);
            }

            return name;
        }
    }
}
