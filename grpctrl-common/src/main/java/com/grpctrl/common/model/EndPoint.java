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
 * Represents the location of the back-end service.
 */
public class EndPoint implements Comparable<EndPoint> {
    @Nonnull
    private String host = "localhost";
    private int port = 5000;
    private boolean secure = true;

    /**
     * Default constructor.
     */
    public EndPoint() {
    }

    /**
     * @param host the host on which the service is running
     * @param port the port on which the service has bound
     * @param secure whether the service is running with SSL/TLS enabled
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if the {@code host} parameter is {@code null}
     */
    public EndPoint(@Nonnull final String host, final int port, final boolean secure) {
        setHost(host);
        setPort(port);
        setSecure(secure);
    }

    /**
     * @param other the end-point to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public EndPoint(@Nonnull EndPoint other) {
        setValues(other);
    }

    /**
     * @param other the end-point to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public EndPoint setValues(@Nonnull EndPoint other) {
        Objects.requireNonNull(other);
        setHost(other.getHost());
        setPort(other.getPort());
        setSecure(other.isSecure());
        return this;
    }

    /**
     * @return the host on which the service is running
     */
    @Nonnull
    public String getHost() {
        return this.host;
    }

    /**
     * @param host the new host on which the service is running
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code host} parameter is {@code null}
     */
    public EndPoint setHost(@Nonnull final String host) {
        this.host = Objects.requireNonNull(host);
        return this;
    }

    /**
     * @return the port on which the service has bound
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @param port the new port on which the service has bound
     *
     * @return {@code this} for fluent-style usage
     */
    public EndPoint setPort(final int port) {
        this.port = port;
        return this;
    }

    /**
     * @return whether the service is running with SSL/TLS enabled
     */
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * @param secure the new value indicating whether the services is running with SSL/TLS enabled
     *
     * @return {@code this} for fluent-style usage
     */
    public EndPoint setSecure(final boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * @return a URL representation capable of being used to communicate with the service
     */
    @Nonnull
    public String asUrl() {
        return String.format("%s://%s:%d/", isSecure() ? "https" : "http", getHost(), getPort());
    }

    @Override
    public int compareTo(@Nullable final EndPoint other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getHost(), other.getHost());
        cmp.append(getPort(), other.getPort());
        cmp.append(isSecure(), other.isSecure());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof EndPoint && compareTo((EndPoint) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getHost());
        hash.append(getPort());
        hash.append(isSecure());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("host", getHost());
        str.append("port", getPort());
        str.append("secure", isSecure());
        return str.build();
    }
}
