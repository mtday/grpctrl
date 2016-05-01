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
        final Tag a = new Tag.Builder("label1", "value1").build();
        final Tag b = new Tag.Builder("label1", "value2").build();
        final Tag c = new Tag.Builder("label2", "value1").build();

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
        final Tag a = new Tag.Builder("label1", "value1").build();
        final Tag b = new Tag.Builder("label1", "value2").build();
        final Tag c = new Tag.Builder("label2", "value1").build();

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
        final Tag a = new Tag.Builder("label1", "value1").build();
        final Tag b = new Tag.Builder("label1", "value2").build();
        final Tag c = new Tag.Builder("label2", "value1").build();

        assertEquals(1040436762, a.hashCode());
        assertEquals(1040436763, b.hashCode());
        assertEquals(1040436799, c.hashCode());
    }

    @Test
    public void testToString() {
        final Tag tag = new Tag.Builder("label", "value").build();
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
    public void testBuilderWithNullLabel() {
        new Tag.Builder(null, "value");
    }

    @SuppressWarnings("all")
    @Test(expected = NullPointerException.class)
    public void testBuilderWithNullValue() {
        new Tag.Builder("label", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongLabel() {
        new Tag.Builder(StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH + 1, "L"), "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithTooLongValue() {
        new Tag.Builder("label", StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH + 1, "V"));
    }

    @Test
    public void testBuilderWithLongLabel() {
        final String label = StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH, "L");
        final Tag tag = new Tag.Builder(label, "value").build();

        assertEquals(label, tag.getLabel());
    }

    @Test
    public void testBuilderWithLongValue() {
        final String value = StringUtils.leftPad("", Tag.Validator.MAX_LABEL_LENGTH, "V");
        final Tag tag = new Tag.Builder("label", value).build();

        assertEquals(value, tag.getValue());
    }

    @Test
    public void testBuilderCopy() {
        final Tag tag = new Tag.Builder("label", "value").build();

        assertEquals(tag, new Tag.Builder(tag).build());
    }
}
