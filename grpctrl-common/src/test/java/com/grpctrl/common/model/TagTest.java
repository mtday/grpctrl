package com.grpctrl.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Perform testing on the {@link Tag} class.
 */
public class TagTest {
    @Test
    public void testCompareTo() {
        final Tag a = new Tag("label1", "value1");
        final Tag b = new Tag("label1", "value2");
        final Tag c = new Tag("label2", "value1");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Tag a = new Tag("label1", "value1");
        final Tag b = new Tag("label1", "value2");
        final Tag c = new Tag("label2", "value1");

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testHashCode() {
        final Tag a = new Tag("label1", "value1");
        final Tag b = new Tag("label1", "value2");
        final Tag c = new Tag("label2", "value1");

        assertEquals(1040436762, a.hashCode());
        assertEquals(1040436763, b.hashCode());
        assertEquals(1040436799, c.hashCode());
    }

    @Test
    public void testToString() {
        final Tag tag = new Tag("label", "value");
        assertEquals("Tag[label=label,value=value]", tag.toString());
    }

    @Test
    public void testValidatorConstructor() {
        // Only here for 100% coverage.
        new Tag.Validator();
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testValidatorWithNullLabel() {
        Tag.Validator.validateLabel(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testValidatorWithNullValue() {
        Tag.Validator.validateValue(null);
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testWithNullLabel() {
        new Tag(null, "value");
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testWithNullValue() {
        new Tag("label", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTooLongLabel() {
        new Tag(StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH + 1, "L"), "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTooLongValue() {
        new Tag("label", StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH + 1, "V"));
    }

    @Test
    public void testWithLongLabel() {
        final String label = StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH, "L");
        final Tag tag = new Tag(label, "value");

        assertEquals(label, tag.getLabel());
    }

    @Test
    public void testWithLongValue() {
        final String value = StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH, "V");
        final Tag tag = new Tag("label", value);

        assertEquals(value, tag.getValue());
    }

    @Test
    public void testCopy() {
        final Tag tag = new Tag("label", "value");

        assertEquals(tag, new Tag(tag));
    }

    @Test
    public void testDefaultConstructor() {
        final Tag tag = new Tag();

        assertEquals("label", tag.getLabel());
        assertEquals("", tag.getValue());
    }
}
