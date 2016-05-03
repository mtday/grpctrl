package com.grpctrl.crypto.common;

import com.grpctrl.crypto.EncryptionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

/**
 * Responsible for defining a common set of encryption capabilities.
 */
public interface CommonEncryption {
    /**
     * Encrypt data from the provided input stream and write to the provided output stream.
     *
     * @param input  an arbitrary byte stream to encrypt
     * @param output the stream to which encrypted data will be written
     * @throws EncryptionException if there is a problem performing the encryption
     */
    void encrypt(@Nonnull InputStream input, @Nonnull OutputStream output) throws EncryptionException;

    /**
     * Encrypt the provided data byte array and return the encrypted data.
     *
     * @param data the data to be encrypted
     * @return the encrypted data
     * @throws EncryptionException if there is a problem performing the encryption
     */
    @Nonnull
    byte[] encrypt(@Nonnull byte[] data) throws EncryptionException;

    /**
     * Encrypt the provided data string and return the encrypted data.
     *
     * @param data the data to be encrypted as a string
     * @param charset the {@link Charset} to use when retrieving bytes from the string
     * @return the encrypted data as a hex string
     * @throws EncryptionException if there is a problem performing the encryption
     */
    @Nonnull
    String encryptString(@Nonnull String data, @Nonnull Charset charset) throws EncryptionException;

    /**
     * Encrypt the provided data string and return the encrypted data suitable for storage as a property in a
     * configuration file (with the {@code ENC{...}} wrapper).
     *
     * @param data the data to be encrypted as a string
     * @param charset the {@link Charset} to use when retrieving bytes from the string
     * @return the encrypted data as a hex string, wrapped with {@code ENC{...}}
     * @throws EncryptionException if there is a problem performing the encryption
     */
    @Nonnull
    String encryptProperty(@Nonnull String data, @Nonnull Charset charset) throws EncryptionException;

    /**
     * Decrypt data from the provided input stream and write to the provided output stream.
     *
     * @param input  an arbitrary byte stream to decrypt
     * @param output the stream to which decrypted data will be written
     * @throws EncryptionException if there is a problem performing the decryption
     */
    void decrypt(@Nonnull InputStream input, @Nonnull OutputStream output) throws EncryptionException;

    /**
     * Decrypt the provided data byte array and return the unencrypted data.
     *
     * @param data the data to be decrypted
     * @return the decrypted data
     * @throws EncryptionException if there is a problem performing the decryption
     */
    @Nonnull
    byte[] decrypt(@Nonnull byte[] data) throws EncryptionException;

    /**
     * Decrypt the provided data string and return the unencrypted data.
     *
     * @param data the data to be decrypted as a hex string
     * @param charset the {@link Charset} to use when recreating the string value
     * @return the decrypted data as a string
     * @throws EncryptionException if there is a problem performing the decryption
     */
    @Nonnull
    String decryptString(@Nonnull String data, @Nonnull Charset charset) throws EncryptionException;

    /**
     * Decrypt the provided data string retrieved from the system configuration properties (which may or may not have
     * the {@code ENC{...}} wrapper) and return the unencrypted data.
     *
     * @param data the data to be decrypted as a hex string, possibly with the {@code ENC{...}} wrapper
     * @param charset the {@link Charset} to use when recreating the string value
     * @return the decrypted data as a string
     * @throws EncryptionException if there is a problem performing the decryption
     */
    @Nonnull
    String decryptProperty(@Nonnull String data, @Nonnull Charset charset) throws EncryptionException;
}
