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
 * Represents a tag assigned to a group or a member, where a tag represents a label and a value. This class is
 * immutable.
 */
public class Tag implements Comparable<Tag> {
    @Nonnull
    private final String label;
    @Nonnull
    private final String value;

    /**
     * Represents a tag assigned to a group or a member, where a tag represents a label and a value.
     *
     * @param label the user-provided label for this tag
     * @param value the user-provided value for this tag
     *
     * @throws NullPointerException if either of the provided parameters is {@code null}
     * @throws IllegalArgumentException if either of the provided parameters is invalid
     */
    public Tag(@Nonnull final String label, @Nonnull final String value) {
        this.label = Validator.validateLabel(label);
        this.value = Validator.validateValue(value);
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
}
