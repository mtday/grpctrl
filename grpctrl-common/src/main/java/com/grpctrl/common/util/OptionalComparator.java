package com.grpctrl.common.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Perform comparisons between two {@link Optional} objects.
 */
@SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "this class contains a "
        + "non-serializable comparator field")
public class OptionalComparator<T> implements Comparator<Optional<T>> {
    @Nullable
    private final Comparator<T> comparator;

    /**
     * Default constructor, uses natural ordering of the elements in the collections.
     */
    public OptionalComparator() {
        this.comparator = null;
    }

    /**
     * @param comparator the {@link Comparator} used to perform comparisons on the objects within the optionals
     */
    public OptionalComparator(@Nonnull final Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator);
    }

    /**
     * @return the {@link Comparator} used to perform comparisons on the objects within the optionals in which case
     *     the natural ordering will be used
     */
    @Nonnull
    protected Optional<Comparator<T>> getComparator() {
        return Optional.ofNullable(this.comparator);
    }

    @Override
    @SuppressWarnings("all")
    public int compare(@Nonnull final Optional<T> first, @Nonnull final Optional<T> second) {
        // Parameters expected to not be null. That is why optionals are used, after all.
        if (first.isPresent() && second.isPresent()) {
            final CompareToBuilder cmp = new CompareToBuilder();
            final Optional<Comparator<T>> comparator = getComparator();
            if (comparator.isPresent()) {
                cmp.append(first.get(), second.get(), comparator.get());
            } else {
                cmp.append(first.get(), second.get());
            }
            return cmp.toComparison();
        } else if (!first.isPresent() && !second.isPresent()) {
            return 0;
        } else if (!first.isPresent()) {
            return -1;
        } else {
            return 1;
        }
    }
}
