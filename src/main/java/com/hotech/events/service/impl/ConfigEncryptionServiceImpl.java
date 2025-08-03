package com.hotech.events.service.impl;

import com.hotech.events.service.ConfigEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 配置加密服务实现类
 * 
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
public class ConfigEncryptionServiceImpl implements ConfigEncryptionService {
    
    @Value("${system.config.encryption.enabled:true}")
    private boolean encryptionEnabled;
    
    @Value("${system.config.encryption.algorithm:AES}")
    private String algorithm;
    
    @Value("${system.config.encryption.key:hotech-config-key-2024}")
    private String encryptionKey;
    
    private static final String ENCRYPTED_PREFIX = "ENC(";
    private static final String ENCRYPTED_SUFFIX = ")";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    @Override
    public String encrypt(String plainText) {
        if (!encryptionEnabled || !StringUtils.hasText(plainText)) {
            return plainText;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), algorithm);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            
            return ENCRYPTED_PREFIX + encryptedText + ENCRYPTED_SUFFIX;
        } catch (Exception e) {
            log.error("加密配置值失败", e);
            return plainText; // 加密失败时返回原值
        }
    }
    
    @Override
    public String decrypt(String cipherText) {
        if (!encryptionEnabled || !isEncrypted(cipherText)) {
            return cipherText;
        }
        
        try {
            // 提取加密内容
            String encryptedContent = cipherText.substring(
                ENCRYPTED_PREFIX.length(), 
                cipherText.length() - ENCRYPTED_SUFFIX.length()
            );
            
            SecretKeySpec secretKey = new SecretKeySpec(
                encryptionKey.getBytes(StandardCharsets.UTF_8), algorithm);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密配置值失败: {}", cipherText, e);
            return cipherText; // 解密失败时返回原值
        }
    }
    
    @Override
    public boolean isEncrypted(String value) {
        return StringUtils.hasText(value) && 
               value.startsWith(ENCRYPTED_PREFIX) && 
               value.endsWith(ENCRYPTED_SUFFIX);
    }
    
    @Override
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(256, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成加密密钥失败", e);
            return null;
        }
    }
}