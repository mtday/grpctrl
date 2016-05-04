package com.grpctrl.common.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
 * Represents a tag assigned to a group or a member, where a tag represents a label and a value. This class is
 * immutable.
 */
@Immutable
@ThreadSafe
public class Tag implements Comparable<Tag> {
    @Nonnull
    private final String label;
    @Nonnull
    private final String value;

    @SuppressWarnings("all")
    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "required by Jackson")
    private Tag() {
        // Required for Jackson deserialization.
        this.label = null;
        this.value = null;
    }

    private Tag(@Nonnull final String label, @Nonnull final String value) {
        // These values have already been validated by the builder.
        this.label = label;
        this.value = value;
    }

    /**
     * @return the label for this tag
     */
    @Nonnull
    public String getLabel() {
        return this.label;
    }

    /**
     * @return the value for this tag
     */
    @Nonnull
    public String getValue() {
        return this.value;
    }

    @Override
    public int compareTo(@Nullable final Tag other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getLabel(), other.getLabel());
        cmp.append(getValue(), other.getValue());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof Tag && compareTo((Tag) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getLabel());
        hash.append(getValue());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("label", getLabel());
        str.append("value", getValue());
        return str.build();
    }

    /**
     * Performs validation on tag fields.
     */
    public static class Validator {
        /** The maximum size of the tag label. */
        public static final int MAX_LABEL_LENGTH = 200;

        /** The maximum size of the tag value. */
        public static final int MAX_VALUE_LENGTH = 200;

        /**
         * Perform validation on the provided {@code label}.
         *
         * @param label the tag label to validate
         *
         * @return the unmodified label, when valid
         *
         * @throws NullPointerException if the provided label is {@code null}
         * @throws IllegalArgumentException if the provided label is invalid
         */
        public static String validateLabel(@Nonnull final String label) {
            Objects.requireNonNull(label);

            if (label.length() > MAX_LABEL_LENGTH) {
                throw new IllegalArgumentException("Invalid tag label, it exceeds the max length: " + MAX_LABEL_LENGTH);
            }

            return label;
        }

        /**
         * Perform validation on the provided {@code value}.
         *
         * @param value the tag value to validate
         *
         * @return the unmodified value, when valid
         *
         * @throws NullPointerException if the provided value is {@code null}
         * @throws IllegalArgumentException if the provided value is invalid
         */
        public static String validateValue(@Nonnull final String value) {
            Objects.requireNonNull(value);

            if (value.length() > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("Invalid tag value, it exceeds the max length: " + MAX_VALUE_LENGTH);
            }

            return value;
        }
    }

    /**
     * Responsible for building tag objects.
     */
    public static class Builder {
        @Nonnull
        private String label = "";

        @Nonnull
        private String value = "";

        /**
         * Create a tag builder instance with the provided label and value.
         *
         * @param label the user-provided label for this tag
         * @param value the user-provided value for this tag
         *
         * @throws NullPointerException if either of the provided parameters is {@code null}
         * @throws IllegalArgumentException if either of the provided parameters is invalid
         */
        public Builder(@Nonnull final String label, @Nonnull final String value) {
            setLabel(label);
            setValue(value);
        }

        /**
         * Create a tag builder instance using the contents of the provided tag for the initial values.
         *
         * @param other the existing tag to duplicate
         *
         * @throws NullPointerException if the provided parameter is {@code null}
         */
        public Builder(@Nonnull final Tag other) {
            setLabel(other.getLabel());
            setValue(other.getValue());
        }

        /**
         * @param label the user-provided label for this tag
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if provided parameter is {@code null}
         * @throws IllegalArgumentException if the provided parameter is invalid
         */
        @Nonnull
        public Builder setLabel(@Nonnull final String label) {
            this.label = Validator.validateLabel(label);
            return this;
        }

        /**
         * @param value the user-provided value for this tag
         *
         * @return {@code this} for fluent-style usage
         *
         * @throws NullPointerException if provided parameter is {@code null}
         * @throws IllegalArgumentException if the provided parameter is invalid
         */
        @Nonnull
        public Builder setValue(@Nonnull final String value) {
            this.value = Validator.validateValue(value);
            return this;
        }

        /**
         * Create the configured tag using the current values in this builder.
         *
         * @return the tag represented by the current values in this builder
         */
        @Nonnull
        public Tag build() {
            return new Tag(this.label, this.value);
        }
    }
}
