package com.grpctrl.crypto.pbe.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.grpctrl.crypto.EncryptionException;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Perform testing on the {@link com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption} class.
 */
public class AESPasswordBasedEncryptionTest {
    @Test
    public void testRoundTripStreamSameAES() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        aes.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        aes.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamMultipleAES() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes1 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes2 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        aes1.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        aes2.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamEmptyAES() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = new byte[0];
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        aes.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        aes.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStreamBufferSizeAES() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = new byte[1024];
        final ByteArrayInputStream originalInput = new ByteArrayInputStream(original);
        final ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();

        aes.encrypt(originalInput, encryptedOutput);
        encryptedOutput.close();

        final ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encryptedOutput.toByteArray());
        final ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream();

        aes.decrypt(encryptedInput, decryptedOutput);
        decryptedOutput.close();

        final byte[] decrypted = decryptedOutput.toByteArray();
        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripByteArraySameAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] encrypted = aes.encrypt(original);
        final byte[] decrypted = aes.decrypt(encrypted);

        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripByteArrayMultipleAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes1 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes2 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final byte[] original = {0x01, 0x02, 0x03, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
        final byte[] encrypted = aes1.encrypt(original);
        final byte[] decrypted = aes2.decrypt(encrypted);

        assertTrue(Objects.deepEquals(original, decrypted));
    }

    @Test
    public void testRoundTripStringSameAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String encrypted = aes.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = aes.decryptString(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test
    public void testRoundTripStringMultipleAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes1 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes2 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String encrypted = aes1.encryptString(original, StandardCharsets.UTF_8);
        final String decrypted = aes2.decryptString(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test
    public void testDecryptPropertyNotEncrypted() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String decrypted = aes.decryptProperty(original, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test
    public void testRoundTripPropertySameAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String encrypted = aes.encryptProperty(original, StandardCharsets.UTF_8);
        final String decrypted = aes.decryptProperty(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test
    public void testRoundTripPropertyMultipleAES() throws EncryptionException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes1 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes2 = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String encrypted = aes1.encryptProperty(original, StandardCharsets.UTF_8);
        final String decrypted = aes2.decryptProperty(encrypted, StandardCharsets.UTF_8);

        assertEquals(original, decrypted);
    }

    @Test(expected = EncryptionException.class)
    public void testEncryptStreamThrowsException() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final ByteArrayInputStream input = Mockito.mock(ByteArrayInputStream.class);
        Mockito.when(input.read(Mockito.any())).thenThrow(new IOException("Failed"));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        aes.encrypt(input, output);
    }

    @Test(expected = EncryptionException.class)
    public void testDecryptStreamThrowsException() throws EncryptionException, IOException {
        final com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption
                aes = new com.grpctrl.crypto.pbe.impl.AESPasswordBasedEncryption("password".toCharArray());

        final String original = "original data";
        final String encrypted = aes.encryptString(original, StandardCharsets.UTF_8);
        final ByteArrayInputStream input = new ByteArrayInputStream(encrypted.getBytes());
        final ByteArrayOutputStream output = Mockito.mock(ByteArrayOutputStream.class);
        Mockito.doThrow(new IOException("Failed")).when(output).write(Mockito.any());

        aes.decrypt(input, output);
    }
}
