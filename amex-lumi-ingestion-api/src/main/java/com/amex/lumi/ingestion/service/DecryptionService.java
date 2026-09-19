package com.amex.lumi.ingestion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class DecryptionService {

    @Value("${encryption.key}")
    private String secretKey;
    public String decrypt(String encryptedValue) {

        if (encryptedValue == null || encryptedValue.isBlank()) {
            return encryptedValue;
        }

        try {
            String[] parts = encryptedValue.split(":", 2);

            byte[] iv =
                    Base64.getDecoder().decode(parts[0]);

            byte[] encryptedData =
                    Base64.getDecoder().decode(parts[1]);

            byte[] keyBytes = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                            secretKey.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            SecretKeySpec key =
                    new SecretKeySpec(keyBytes, "AES");

            Cipher cipher =
                    Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(128, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    parameterSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encryptedData);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to decrypt employee data",
                    exception
            );
        }
    }

}