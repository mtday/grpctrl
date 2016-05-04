package com.grpctrl.common.model;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the location of the back-end service. This is an immutable class.
 */
public class EndPoint implements Comparable<EndPoint> {
    @Nonnull
    private final String host;
    private final int port;
    private final boolean secure;

    private EndPoint(@Nonnull final String host, final int port, final boolean secure) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.secure = secure;
    }

    /**
     * @return the host on which the service is running
     */
    @Nonnull
    public String getHost() {
        return this.host;
    }

    /**
     * @return the port on which the service has bound
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return whether the service is running with SSL/TLS enabled
     */
    public boolean isSecure() {
        return this.secure;
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

    /**
     * Responsible for building end-point objects.
     */
    public static class Builder {
        @Nonnull
        private String host = "localhost";
        private int port = 5000;
        private boolean secure = true;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * @param other the end-point to duplicate
         *
         * @throws NullPointerException if the parameter is {@code null}
         */
        public Builder(@Nonnull final EndPoint other) {
            Objects.requireNonNull(other);

            setHost(other.getHost());
            setPort(other.getPort());
            setSecure(other.isSecure());
        }

        /**
         * @param host the server host name
         * @param port the server port number
         * @param secure whether the server is running in secure HTTPS mode
         *
         * @throws NullPointerException if the {@code host} parameter is {@code null}
         * @throws IllegalArgumentException if any of the parameters are invalid
         */
        public Builder(@Nonnull final String host, final int port, final boolean secure) {
            setHost(Objects.requireNonNull(host));
            setPort(port);
            setSecure(secure);
        }

        /**
         * @param host the new host value
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder setHost(@Nonnull final String host) {
            Objects.requireNonNull(host);
            Preconditions.checkArgument(!StringUtils.isBlank(host), "Host name cannot be empty or blank");
            this.host = host;
            return this;
        }

        /**
         * @param port the new port value
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder setPort(final int port) {
            Preconditions.checkArgument(port > 0, "Port number must be positive");
            this.port = port;
            return this;
        }

        /**
         * @param secure the new secure value indicating whether HTTPS should be used
         * @return {@code this} for fluent-style usage
         */
        @Nonnull
        public Builder setSecure(final boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * @return the end-point represented by this builder
         */
        @Nonnull
        public EndPoint build() {
            return new EndPoint(this.host, this.port, this.secure);
        }
    }
}
