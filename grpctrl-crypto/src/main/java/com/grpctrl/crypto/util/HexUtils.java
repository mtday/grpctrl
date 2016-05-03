package com.grpctrl.crypto.util;

/**
 * Provides some hex processing utility methods.
 */
public final class HexUtils {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * @param bytes the byte array to be converted into a string of hex characters
     * @return the string of hex characters
     */
    public static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @param hex the hex string to convert into a byte array
     * @return the byte array represented by the hex string
     */
    public static byte[] hexToBytes(final String hex) {
        final int len = hex.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
