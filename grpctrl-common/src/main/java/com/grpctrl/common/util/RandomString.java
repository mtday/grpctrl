package com.grpctrl.common.util;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Random;

/**
 * Generates random strings.
 */
public class RandomString {
    /**
     * @param length the length of the password to generate
     *
     * @return a randomly generated password of the specified length
     *
     * @throws IllegalArgumentException if the provided length is not valid
     */
    public static String get(final int length) {
        isTrue(length >= 0 && length <= 255, "Invalid length: " + length);

        final String chars = "aeuAEU23456789bdghjmnpqrstvzBDGHJLMNPQRSTVWXZ";

        final Random random = new Random();
        final StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
