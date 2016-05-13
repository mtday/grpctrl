package com.grpctrl.crypto.ske.impl;

import com.grpctrl.crypto.common.CommonEncryptionImpl;
import com.grpctrl.crypto.ske.SymmetricKeyEncryption;
import com.grpctrl.crypto.util.HexUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.InternalServerErrorException;

/**
 * Provides an implementation of the {@link SymmetricKeyEncryption} interface, using the the AES algorithm to perform
 * the encryption and decryption of data (with the key encrypted using the symmetric-key algorithm), along with using
 * the provided {@link KeyPair} to perform symmetric-key encryption, decryption, and signing operations.
 */
public class AESSymmetricKeyEncryption extends CommonEncryptionImpl implements SymmetricKeyEncryption {
    private static final String ALGORITHM = "AES";

    @Nonnull
    private final KeyPair keyPair;

    /**
     * @param keyPair the {@link KeyPair} containing the public and private symmetric keys
     */
    public AESSymmetricKeyEncryption(@Nonnull final KeyPair keyPair) {
        this.keyPair = Objects.requireNonNull(keyPair);
    }

    /**
     * @param keyLength the length of the key to generate
     * @return the {@link SecretKey} used to do the encryption and decryption of system data
     */
    protected SecretKey createSecretKey(final int keyLength) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keyLength);
            return keyGenerator.generateKey();
        } catch (final InvalidParameterException | NoSuchAlgorithmException badAlgorithm) {
            throw new InternalServerErrorException("Failed to generate secret key for encryption", badAlgorithm);
        }
    }

    /**
     * @param secretKey the {@link SecretKey} to encrypt
     * @return the bytes representing the encrypted secret key
     */
    @Nonnull
    protected byte[] getEncryptedSecretKey(@Nonnull final SecretKey secretKey) throws Exception {
        Objects.requireNonNull(secretKey);
        final PrivateKey privateKey = this.keyPair.getPrivate();
        final Cipher symmetricCipher = Cipher.getInstance(privateKey.getAlgorithm());
        symmetricCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return symmetricCipher.doFinal(secretKey.getEncoded());
    }

    /**
     * @param encrypted the encrypted secret key
     * @return the {@link SecretKeySpec} decrypted from the provided secret key encrypted bytes
     */
    @Nonnull
    protected SecretKeySpec getDecryptedSecretKey(@Nonnull final byte[] encrypted) throws Exception {
        Objects.requireNonNull(encrypted);
        final PublicKey publicKey = this.keyPair.getPublic();
        final Cipher symmetricCipher = Cipher.getInstance(publicKey.getAlgorithm());
        symmetricCipher.init(Cipher.DECRYPT_MODE, publicKey);
        return new SecretKeySpec(symmetricCipher.doFinal(encrypted), ALGORITHM);
    }

    @Override
    public void encrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        try {
            final int keyLength = 128; // The unlimited-strength jce not required for this.
            final SecretKey secretKey = createSecretKey(keyLength);
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), ALGORITHM));

            // First, write the secret key into the output stream
            final byte[] encryptedSecretKey = getEncryptedSecretKey(secretKey);
            output.write(encryptedSecretKey.length / 8);
            output.write(encryptedSecretKey);

            // Read data from input into buffer, encrypt and write to output
            apply(cipher, input, output);
        } catch (final Exception exception) {
            throw new InternalServerErrorException("Failed to encrypt data", exception);
        }
    }

    @Override
    public void decrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        try {
            // Read the encrypted key value
            final int encryptedKeyLength = input.read() * 8;
            final byte[] encryptedKey = new byte[encryptedKeyLength];
            int keyRead = 0;
            while (keyRead < encryptedKeyLength) {
                keyRead += input.read(encryptedKey, keyRead, encryptedKeyLength - keyRead);
            }

            final SecretKeySpec secretKey = getDecryptedSecretKey(encryptedKey);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Read data from input into buffer, decrypt and write to output
            apply(cipher, input, output);
        } catch (final Exception exception) {
            throw new InternalServerErrorException("Failed to decrypt data", exception);
        }
    }

    @Override
    @Nonnull
    public byte[] sign(@Nonnull final byte[] data) {
        Objects.requireNonNull(data);
        try {
            final PrivateKey privateKey = this.keyPair.getPrivate();
            final Signature signature = Signature.getInstance("SHA1with" + privateKey.getAlgorithm());
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (final NoSuchAlgorithmException | SignatureException | InvalidKeyException exception) {
            throw new InternalServerErrorException("Failed to sign data", exception);
        }
    }

    @Override
    @Nonnull
    public String signString(@Nonnull final String data, @Nonnull final Charset charset) {
        return HexUtils.bytesToHex(sign(Objects.requireNonNull(data).getBytes(Objects.requireNonNull(charset))));
    }

    @Override
    public boolean verify(@Nonnull final byte[] data, @Nonnull final byte[] signatureData) {
        Objects.requireNonNull(data);
        try {
            final PublicKey publicKey = this.keyPair.getPublic();
            final Signature signature = Signature.getInstance("SHA1with" + publicKey.getAlgorithm());
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureData);
        } catch (final NoSuchAlgorithmException | SignatureException | InvalidKeyException exception) {
            throw new InternalServerErrorException("Failed to verify signature", exception);
        }
    }

    @Override
    public boolean verifyString(
            @Nonnull final String data, @Nonnull final Charset charset, @Nonnull final String signatureData) {
        final byte[] dataBytes = Objects.requireNonNull(data).getBytes(Objects.requireNonNull(charset));
        return verify(dataBytes, HexUtils.hexToBytes(signatureData));
    }
}
