package com.inventory.token.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.profiles.active=test"
})
class EncryptionServiceTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    void testEncryptDecrypt() {
        String originalText = "test password 123";
        
        String encrypted = encryptionService.encrypt(originalText);
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);
        
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncryptNull() {
        String encrypted = encryptionService.encrypt(null);
        assertNull(encrypted);
    }

    @Test
    void testDecryptNull() {
        String decrypted = encryptionService.decrypt(null);
        assertNull(decrypted);
    }

    @Test
    void testIsEncrypted() {
        String plainText = "plain text";
        String encryptedText = encryptionService.encrypt(plainText);
        
        assertFalse(encryptionService.isEncrypted(plainText));
        assertTrue(encryptionService.isEncrypted(encryptedText));
    }
}
