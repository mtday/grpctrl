package com.grpctrl.common.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Perform comparisons between two {@link Collection} objects.
 */
@SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
        justification = "this class contains a non-serializable comparator field")
public class CollectionComparator<T> implements Comparator<Collection<T>> {
    @Nullable
    private final Comparator<T> comparator;

    /**
     * Default constructor, uses natural ordering of the elements in the collections.
     */
    public CollectionComparator() {
        this.comparator = null;
    }

    /**
     * Parameter constructor.
     *
     * @param comparator the {@link Comparator} used to perform comparisons on the objects within the collections
     */
    public CollectionComparator(@Nonnull final Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator);
    }

    /**
     * @return the {@link Comparator} used to perform comparisons on the objects within the collections, possibly empty
     *     in which case the natural ordering will be used
     */
    @Nonnull
    protected Optional<Comparator<T>> getComparator() {
        return Optional.ofNullable(this.comparator);
    }

    @Override
    public int compare(@Nullable final Collection<T> first, @Nullable final Collection<T> second) {
        if (first != null && second != null) {
            final CompareToBuilder cmp = new CompareToBuilder();
            final Iterator<T> iterA = first.iterator();
            final Iterator<T> iterB = second.iterator();

            final Optional<Comparator<T>> comparatorOptional = getComparator();
            while (cmp.toComparison() == 0 && iterA.hasNext() && iterB.hasNext()) {
                if (comparatorOptional.isPresent()) {
                    cmp.append(iterA.next(), iterB.next(), comparatorOptional.get());
                } else {
                    cmp.append(iterA.next(), iterB.next());
                }
            }

            if (cmp.toComparison() == 0) {
                if (iterA.hasNext()) {
                    return 1;
                } else if (iterB.hasNext()) {
                    return -1;
                }
            }

            return cmp.toComparison();
        } else if (first == null && second == null) {
            return 0;
        } else if (first == null) {
            return 1;
        } else {
            return -1;
        }
    }
}
