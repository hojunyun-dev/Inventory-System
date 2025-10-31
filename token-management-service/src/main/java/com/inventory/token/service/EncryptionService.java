package com.inventory.token.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    
    private final String encryptionKey;
    private final BytesEncryptor encryptor;
    
    public EncryptionService(@Value("${encryption.key}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
        this.encryptor = Encryptors.standard(encryptionKey, KeyGenerators.string().generateKey());
    }
    
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            byte[] encrypted = encryptor.encrypt(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = encryptor.decrypt(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
    
    public boolean isEncrypted(String text) {
        try {
            Base64.getDecoder().decode(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
