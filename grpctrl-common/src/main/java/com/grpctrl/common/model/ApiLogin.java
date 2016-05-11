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
 * Defines the security login information required to perform REST operations against the API.
 */
public class ApiLogin implements Comparable<ApiLogin> {
    @Nonnull
    private String key = "";
    @Nonnull
    private String secret = "";

    /**
     * Default constructor.
     */
    public ApiLogin() {
    }

    /**
     * @param key the API login key
     * @param secret the API login secret
     *
     * @throws NullPointerException if either of the provided parameters are {@code null}
     */
    public ApiLogin(@Nonnull final String key, @Nonnull final String secret) {
        setKey(key);
        setSecret(secret);
    }

    /**
     * @param other the API login to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public ApiLogin(@Nonnull final ApiLogin other) {
        setValues(other);
    }

    /**
     * @param other the API login to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public void setValues(@Nonnull final ApiLogin other) {
        Objects.requireNonNull(other);
        setKey(other.getKey());
        setSecret(other.getSecret());
    }

    /**
     * @return the API login key
     */
    @Nonnull
    public String getKey() {
        return this.key;
    }

    /**
     * @param key the new API login key
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code key} parameter is {@code null}
     */
    @Nonnull
    public ApiLogin setKey(@Nonnull final String key) {
        this.key = Objects.requireNonNull(key);
        return this;
    }

    /**
     * @return the API login secret
     */
    @Nonnull
    public String getSecret() {
        return this.secret;
    }

    /**
     * @param secret the new API login secret
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public ApiLogin setSecret(@Nonnull final String secret) {
        this.secret = Objects.requireNonNull(secret);
        return this;
    }

    @Override
    public int compareTo(@Nullable final ApiLogin other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getKey(), other.getKey());
        cmp.append(getSecret(), other.getSecret());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof ApiLogin && compareTo((ApiLogin) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getKey());
        hash.append(getSecret());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("key", getKey());
        str.append("secret", getSecret());
        return str.build();
    }
}
