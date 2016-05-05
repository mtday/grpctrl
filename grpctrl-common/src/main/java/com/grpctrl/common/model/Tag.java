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
 * Represents a tag assigned to a group or a member, where a tag represents a label and a value.
 */
public class Tag implements Comparable<Tag> {
    @Nonnull
    private String label = "label";
    @Nonnull
    private String value = "";

    /**
     * Default constructor.
     */
    public Tag() {
    }

    /**
     * @param label the label for this tag
     * @param value the value for this tag
     *
     * @throws IllegalArgumentException if any of the parameters are invalid
     * @throws NullPointerException if any of the parameters are {@code null}
     */
    public Tag(@Nonnull final String label, @Nonnull final String value) {
        setLabel(label);
        setValue(value);
    }

    /**
     * @param other the tag to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Tag(@Nonnull final Tag other) {
        setValues(other);
    }

    /**
     * @param other the tag to duplicate
     *
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Tag setValues(@Nonnull final Tag other) {
        Objects.requireNonNull(other);
        setLabel(other.getLabel());
        setValue(other.getValue());
        return this;
    }

    /**
     * @return the label for this tag
     */
    @Nonnull
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label the new label for this tag
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the parameter is invalid
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Tag setLabel(@Nonnull final String label) {
        this.label = Validator.validateLabel(label);
        return this;
    }

    /**
     * @return the value for this tag
     */
    @Nonnull
    public String getValue() {
        return this.value;
    }

    /**
     * @param value the new value for this tag
     *
     * @return {@code this} for fluent-style usage
     *
     * @throws IllegalArgumentException if the parameter is invalid
     * @throws NullPointerException if the parameter is {@code null}
     */
    public Tag setValue(@Nonnull final String value) {
        this.value = Validator.validateValue(value);
        return this;
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
