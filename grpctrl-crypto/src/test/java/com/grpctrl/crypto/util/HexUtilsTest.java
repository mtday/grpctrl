package com.grpctrl.crypto.util;

import static org.junit.Assert.assertEquals;

import com.grpctrl.crypto.util.HexUtils;

import org.junit.Test;

/**
 * Perform testing on the {@link HexUtils} class.
 */
public class HexUtilsTest {
    @Test
    public void testConstructor() {
        new HexUtils(); // Just here for 100% coverage
    }

    @Test
    public void testRoundTrip() {
        final String original = "abcd1234ABCD";

        final byte[] bytes = HexUtils.hexToBytes(original);
        final String hex = HexUtils.bytesToHex(bytes);

        assertEquals(original.toLowerCase(), hex);
    }
}