package com.grpctrl.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.Optional;

/**
 * Perform testing of the {@link OptionalComparatorTest} class.
 */
public class OptionalComparatorTest {
    @Test
    public void testCompareBothEmpty() {
        final Optional<Integer> a = Optional.empty();
        final Optional<Integer> b = Optional.empty();

        Assert.assertEquals(0, new OptionalComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareFirstEmpty() {
        final Optional<Integer> a = Optional.empty();
        final Optional<Integer> b = Optional.of(1);

        Assert.assertEquals(-1, new OptionalComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareSecondEmpty() {
        final Optional<Integer> a = Optional.of(1);
        final Optional<Integer> b = Optional.empty();

        Assert.assertEquals(1, new OptionalComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareBothWithValues() {
        final Optional<Integer> a = Optional.of(1);
        final Optional<Integer> b = Optional.of(1);

        Assert.assertEquals(0, new OptionalComparator<Integer>().compare(a, b));
    }

    @Test
    public void testCompareCustomComparator() {
        final Comparator<Integer> customComparator = (a, b) -> (new Integer(Math.abs(a)).compareTo(Math.abs(b)));

        final Optional<Integer> a = Optional.of(-1);
        final Optional<Integer> b = Optional.of(1);

        Assert.assertEquals(0, new OptionalComparator<>(customComparator).compare(a, b));
    }
}
