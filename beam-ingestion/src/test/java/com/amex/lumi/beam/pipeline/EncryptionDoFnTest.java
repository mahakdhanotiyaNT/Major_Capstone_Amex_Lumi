package com.amex.lumi.beam.pipeline;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptionDoFnTest {

    @Test
    void shouldEncryptValue() throws Exception {

        EncryptionDoFn encryptionDoFn =
                new EncryptionDoFn("test-secret-key");

        Method encryptMethod =
                EncryptionDoFn.class.getDeclaredMethod(
                        "encrypt",
                        String.class
                );

        encryptMethod.setAccessible(true);

        String encrypted =
                (String) encryptMethod.invoke(
                        encryptionDoFn,
                        "9876543210"
                );

        assertNotNull(encrypted);
        assertNotEquals("9876543210", encrypted);
        assertTrue(encrypted.contains(":"));
    }

    @Test
    void shouldGenerateDifferentCipherTextForSameValue() throws Exception {

        EncryptionDoFn encryptionDoFn =
                new EncryptionDoFn("test-secret-key");

        Method encryptMethod =
                EncryptionDoFn.class.getDeclaredMethod(
                        "encrypt",
                        String.class
                );

        encryptMethod.setAccessible(true);

        String first =
                (String) encryptMethod.invoke(
                        encryptionDoFn,
                        "50000"
                );

        String second =
                (String) encryptMethod.invoke(
                        encryptionDoFn,
                        "50000"
                );

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnNullForNullValue() throws Exception {

        EncryptionDoFn encryptionDoFn =
                new EncryptionDoFn("test-secret-key");

        Method encryptMethod =
                EncryptionDoFn.class.getDeclaredMethod(
                        "encrypt",
                        String.class
                );

        encryptMethod.setAccessible(true);

        String result =
                (String) encryptMethod.invoke(
                        encryptionDoFn,
                        (String) null
                );

        assertEquals(null, result);
    }

    @Test
    void shouldReturnBlankValueWithoutEncryption() throws Exception {

        EncryptionDoFn encryptionDoFn =
                new EncryptionDoFn("test-secret-key");

        Method encryptMethod =
                EncryptionDoFn.class.getDeclaredMethod(
                        "encrypt",
                        String.class
                );

        encryptMethod.setAccessible(true);

        String result =
                (String) encryptMethod.invoke(
                        encryptionDoFn,
                        " "
                );

        assertEquals(" ", result);
    }
}