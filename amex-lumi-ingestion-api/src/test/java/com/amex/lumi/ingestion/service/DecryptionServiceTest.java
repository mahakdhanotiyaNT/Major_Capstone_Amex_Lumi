package com.amex.lumi.ingestion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecryptionServiceTest {

    private final DecryptionService decryptionService =
            new DecryptionService();

    private String testKey;

    @BeforeEach
    void setUp() {

        testKey = "test-key-for-unit-tests";

        ReflectionTestUtils.setField(
                decryptionService,
                "secretKey",
                testKey
        );
    }

    @Test
    void shouldDecryptEncryptedValue() throws Exception {

        String encryptedValue =
                encryptForTest("9876543210");

        String result =
                decryptionService.decrypt(encryptedValue);

        assertEquals("9876543210", result);
    }

    @Test
    void shouldReturnNullForNullValue() {

        assertNull(
                decryptionService.decrypt(null)
        );
    }

    @Test
    void shouldReturnBlankValueWithoutDecryption() {

        assertEquals(
                "",
                decryptionService.decrypt("")
        );
    }

    @Test
    void shouldThrowExceptionForInvalidEncryptedValue() {

        assertThrows(
                IllegalStateException.class,
                () -> decryptionService.decrypt("invalid-value")
        );
    }

    @Test
    void shouldThrowExceptionForTamperedEncryptedValue()
            throws Exception {

        String encryptedValue =
                encryptForTest("9876543210");

        String[] parts =
                encryptedValue.split(":", 2);

        byte[] encryptedData =
                Base64.getDecoder().decode(parts[1]);

        encryptedData[encryptedData.length - 1] ^= 1;

        String tamperedValue =
                parts[0]
                        + ":"
                        + Base64.getEncoder()
                        .encodeToString(encryptedData);

        assertThrows(
                IllegalStateException.class,
                () -> decryptionService.decrypt(tamperedValue)
        );
    }

    private String encryptForTest(String value)
            throws Exception {

        byte[] keyBytes =
                MessageDigest
                        .getInstance("SHA-256")
                        .digest(
                                testKey.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        SecretKeySpec key =
                new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        Cipher cipher =
                Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec parameterSpec =
                new GCMParameterSpec(128, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                parameterSpec
        );

        byte[] encrypted =
                cipher.doFinal(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return Base64.getEncoder().encodeToString(iv)
                + ":"
                + Base64.getEncoder()
                .encodeToString(encrypted);
    }
}