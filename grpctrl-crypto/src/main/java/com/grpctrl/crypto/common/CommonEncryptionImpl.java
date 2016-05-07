package com.grpctrl.crypto.common;

import com.grpctrl.crypto.util.HexUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;

/**
 * Provides a base implementation of the {@link CommonEncryption} interface.
 */
public abstract class CommonEncryptionImpl implements CommonEncryption {
    // Process input/output streams in chunks - arbitrary
    private static final int BUFFER_SIZE = 1024;

    @Override
    public abstract void encrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output);

    @Override
    @Nonnull
    public byte[] encrypt(@Nonnull final byte[] data) {
        Objects.requireNonNull(data);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        encrypt(new ByteArrayInputStream(data), output);
        return output.toByteArray();
    }

    @Override
    @Nonnull
    public String encryptString(@Nonnull final String data, @Nonnull final Charset charset) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(charset);
        return HexUtils.bytesToHex(encrypt(data.getBytes(charset)));
    }

    @Override
    @Nonnull
    public String encryptProperty(@Nonnull final String data, @Nonnull final Charset charset) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(charset);
        return String.format("ENC{%s}", HexUtils.bytesToHex(encrypt(data.getBytes(charset))));
    }

    @Override
    public abstract void decrypt(@Nonnull final InputStream input, @Nonnull final OutputStream output);

    @Override
    @Nonnull
    public byte[] decrypt(@Nonnull final byte[] data) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        decrypt(new ByteArrayInputStream(Objects.requireNonNull(data)), output);
        return output.toByteArray();
    }

    @Override
    @Nonnull
    public String decryptString(@Nonnull final String data, @Nonnull final Charset charset) {
        return new String(decrypt(HexUtils.hexToBytes(Objects.requireNonNull(data))), Objects.requireNonNull(charset));
    }

    @Override
    @Nonnull
    public String decryptProperty(@Nonnull final String data, @Nonnull final Charset charset) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(charset);
        if (data.matches("ENC\\{.*\\}")) {
            final String encrypted = data.substring(4, data.length() - 1);
            return new String(decrypt(HexUtils.hexToBytes(encrypted)), charset);
        }
        return data;
    }

    public void apply(
            @Nonnull final Cipher cipher, @Nonnull final InputStream input, @Nonnull final OutputStream output)
            throws Exception {
        Objects.requireNonNull(cipher);
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);

        // Read data from input into buffer, apply the cipher, and write to output
        final byte[] buffer = new byte[BUFFER_SIZE];
        int numRead;
        boolean more = true;
        while (more) {
            numRead = input.read(buffer);
            more = numRead >= 0;
            if (more) {
                output.write(cipher.update(buffer, 0, numRead));
            }
        }
        output.write(cipher.doFinal());
    }
}
