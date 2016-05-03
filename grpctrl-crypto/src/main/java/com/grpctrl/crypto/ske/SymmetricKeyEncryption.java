package com.grpctrl.crypto.ske;

import com.grpctrl.crypto.EncryptionException;
import com.grpctrl.crypto.common.CommonEncryption;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;

/**
 * Responsible for performing symmetric-key encryption operations.
 */
public interface SymmetricKeyEncryption extends CommonEncryption {
    /**
     * Sign the provided data byte array and return the signature value.
     *
     * @param data the data to be signed
     * @return the signature describing the signed data
     * @throws EncryptionException if there is a problem performing the signing
     */
    @Nonnull
    byte[] sign(@Nonnull byte[] data) throws EncryptionException;

    /**
     * Sign the provided data string and return the signature value.
     *
     * @param data the data to be signed, as a string
     * @param charset the {@link Charset} to use when retrieving the bytes from the string value
     * @return the signature describing the signed data
     * @throws EncryptionException if there is a problem performing the signing
     */
    @Nonnull
    String signString(@Nonnull String data, @Nonnull Charset charset) throws EncryptionException;

    /**
     * Verify the provided data byte array and signature
     *
     * @param data the data that has been signed
     * @param signature the signature of the data to verify
     * @return whether the provided signature matches the expected signature for the provided data
     * @throws EncryptionException if there is a problem performing the verification
     */
    boolean verify(@Nonnull byte[] data, @Nonnull byte[] signature) throws EncryptionException;

    /**
     * Verify the provided data byte array and signature
     *
     * @param data the data that has been signed
     * @param charset the {@link Charset} to use when retrieving the bytes from the string value
     * @param signature the signature of the data to verify
     * @return whether the provided signature matches the expected signature for the provided data
     * @throws EncryptionException if there is a problem performing the verification
     */
    boolean verifyString(@Nonnull String data, @Nonnull Charset charset, @Nonnull String signature)
            throws EncryptionException;
}
