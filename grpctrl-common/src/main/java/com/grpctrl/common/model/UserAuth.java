package com.grpctrl.common.model;

import static org.apache.commons.lang3.Validate.isTrue;

import com.google.common.base.Charsets;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides information about user's password-based login.
 */
public class UserAuth implements Comparable<UserAuth> {
    @Nonnull
    private String hashAlgorithm = "SHA-512";

    @Nonnull
    private String salt = "";

    @Nonnull
    private String hashedPass = "";

    /**
     * Default constructor.
     */
    public UserAuth() {
    }

    /**
     * @param hashAlgorithm the hash algorithm used to generate the hashed password
     * @param salt the salt value incorporated into the password prior to hashing
     * @param hashedPass the hashed password value
     *
     * @throws NullPointerException if any of the provided parameters are {@code null}
     */
    public UserAuth(@Nonnull final String hashAlgorithm, @Nonnull final String salt, @Nonnull final String hashedPass) {
        setHashAlgorithm(hashAlgorithm);
        setSalt(salt);
        setHashedPass(hashedPass);
    }

    /**
     * @param other the user authorization to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public UserAuth(@Nonnull final UserAuth other) {
        setValues(other);
    }

    /**
     * @param other the user authorization to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public void setValues(@Nonnull final UserAuth other) {
        Objects.requireNonNull(other);
        setHashAlgorithm(other.getHashAlgorithm());
        setSalt(other.getSalt());
        setHashedPass(other.getHashedPass());
    }

    /**
     * Create a new {@link UserAuth} based on the provided password.
     *
     * @param password the plain-text password for which a new {@link UserAuth} should be created
     *
     * @return the requested {@link UserAuth}
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public static UserAuth fromPassword(@Nonnull final String password) {
        Objects.requireNonNull(password);

        try {
            final String hashAlgorithm = "SHA-512";
            final MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            final String salt = randomString(8);
            final String hashedPass = toHexString(messageDigest.digest((salt + password).getBytes(Charsets.UTF_8)));

            return new UserAuth(hashAlgorithm, salt, hashedPass);
        } catch (final NoSuchAlgorithmException badHashAlgorithm) {
            // Not expecting this to happen.
            throw new RuntimeException("Unexpected bad algorithm", badHashAlgorithm);
        }
    }

    /**
     * @param length the length of the password to generate
     *
     * @return a randomly generated password of the specified length
     *
     * @throws IllegalArgumentException if the provided length is not valid
     */
    public static String randomString(final int length) {
        isTrue(length >= 0 && length <= 255, "Invalid length: " + length);

        final String chars = "aeuAEU23456789bdghjmnpqrstvzBDGHJLMNPQRSTVWXZ";

        final Random random = new Random();
        final StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * Create a string representation of the provided byte array.
     *
     * @param bytes the byte array to convert into a string representation
     *
     * @return the bytes as a hex string
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public static String toHexString(@Nonnull final byte[] bytes) {
        Objects.requireNonNull(bytes);

        final char[] hex = "0123456789abcdef".toCharArray();
        final StringBuilder sb = new StringBuilder(bytes.length << 1);

        for (final byte b : bytes) {
            sb.append(hex[(b & 0xf0) >> 4]).append(hex[b & 0x0f]);
        }

        return sb.toString();
    }

    /**
     * Validate that the password is correct for this user authorization.
     *
     * @param password the plain-text password to validate
     *
     * @return whether the password matches what is expected for this user authorization
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public boolean validate(@Nonnull final String password) {
        Objects.requireNonNull(password);

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(getHashAlgorithm());
            final String expected = toHexString(messageDigest.digest((getSalt() + password).getBytes(Charsets.UTF_8)));
            return getHashedPass().equals(expected);
        } catch (final NoSuchAlgorithmException badHashAlgorithm) {
            // Not expecting this to happen.
            throw new RuntimeException("Unexpected bad algorithm", badHashAlgorithm);
        }
    }

    /**
     * @return the hash algorithm used to hash the password and salt
     */
    @Nonnull
    public String getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    /**
     * @param hashAlgorithm the new hash algorithm value
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code hashAlgorithm} parameter is {@code null}
     */
    @Nonnull
    public UserAuth setHashAlgorithm(@Nonnull final String hashAlgorithm) {
        this.hashAlgorithm = Objects.requireNonNull(hashAlgorithm);
        return this;
    }

    /**
     * @return the salt used in conjunction with the user password in the hash
     */
    @Nonnull
    public String getSalt() {
        return this.salt;
    }

    /**
     * @param salt the new salt value used in conjunction with the user password in the hash
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public UserAuth setSalt(@Nonnull final String salt) {
        this.salt = Objects.requireNonNull(salt);
        return this;
    }

    /**
     * @return the hashed password value
     */
    @Nonnull
    public String getHashedPass() {
        return this.hashedPass;
    }

    /**
     * @param hashedPass the new hashed password value
     * @return {@code this} for fluent-style usage
     */
    @Nonnull
    public UserAuth setHashedPass(@Nonnull final String hashedPass) {
        this.hashedPass = Objects.requireNonNull(hashedPass);
        return this;
    }

    @Override
    public int compareTo(@Nullable final UserAuth other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(other.getSalt(), getSalt());
        cmp.append(getHashAlgorithm(), other.getHashAlgorithm());
        cmp.append(other.getHashedPass(), getHashedPass());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof UserAuth && compareTo((UserAuth) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getHashAlgorithm());
        hash.append(getSalt());
        hash.append(getHashedPass());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("hashAlgorithm", getHashAlgorithm());
        str.append("salt", getSalt());
        str.append("hashedPass", getHashedPass());
        return str.build();
    }
}
