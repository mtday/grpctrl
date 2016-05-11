package com.grpctrl.common.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides information about user email addresses as provided by the security authorization service.
 */
public class UserEmail implements Comparable<UserEmail> {
    @Nonnull
    private String email = "";

    private boolean primary = true;
    private boolean verified = true;

    /**
     * Default constructor.
     */
    public UserEmail() {
    }

    /**
     * @param email the user email address
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public UserEmail(@Nonnull final String email) {
        this(email, true, true);
    }

    /**
     * @param email the user email address
     * @param primary whether the email address is the primary address for the account
     * @param verified whether the email address has been verified
     *
     * @throws NullPointerException if the provided {@code email} parameter is {@code null}
     */
    public UserEmail(@Nonnull final String email, final boolean primary, final boolean verified) {
        setEmail(email);
        setPrimary(primary);
        setVerified(verified);
    }

    /**
     * @param other the email address to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public UserEmail(@Nonnull final UserEmail other) {
        setValues(other);
    }

    /**
     * @param other the email address to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public void setValues(@Nonnull final UserEmail other) {
        Objects.requireNonNull(other);
        setEmail(other.getEmail());
        setPrimary(other.isPrimary());
        setVerified(other.isVerified());
    }

    /**
     * @return the user email address
     */
    @Nonnull
    public String getEmail() {
        return this.email;
    }

    /**
     * @param email the new user email address
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code email} parameter is {@code null}
     */
    @Nonnull
    public UserEmail setEmail(@Nonnull final String email) {
        this.email = Objects.requireNonNull(email);
        return this;
    }

    /**
     * @return whether the email is the primary email for the user
     */
    public boolean isPrimary() {
        return this.primary;
    }

    /**
     * @param primary the new value indicating whether the email is the primary email for the user
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public UserEmail setPrimary(final boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * @return whether the email has been verified by the user
     */
    public boolean isVerified() {
        return this.verified;
    }

    /**
     * @param verified the new value indicating whether the email address has been verified
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public UserEmail setVerified(final boolean verified) {
        this.verified = verified;
        return this;
    }

    @Override
    public int compareTo(@Nullable final UserEmail other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(other.isPrimary(), isPrimary());
        cmp.append(getEmail(), other.getEmail());
        cmp.append(other.isVerified(), isVerified());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof UserEmail && compareTo((UserEmail) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getEmail());
        hash.append(isPrimary());
        hash.append(isVerified());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("email", getEmail());
        str.append("primary", isPrimary());
        str.append("verified", isVerified());
        return str.build();
    }
}
