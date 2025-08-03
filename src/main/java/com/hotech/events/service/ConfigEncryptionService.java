package com.hotech.events.service;

/**
 * 配置加密服务接口
 * 负责敏感配置的加密和解密
 * 
 * @author system
 * @since 2025-01-24
 */
public interface ConfigEncryptionService {
    
    /**
     * 加密配置值
     * @param plainText 明文
     * @return 密文
     */
    String encrypt(String plainText);
    
    /**
     * 解密配置值
     * @param cipherText 密文
     * @return 明文
     */
    String decrypt(String cipherText);
    
    /**
     * 判断是否为加密值
     * @param value 配置值
     * @return 是否加密
     */
    boolean isEncrypted(String value);
    
    /**
     * 生成新的加密密钥
     * @return 密钥
     */
    String generateKey();
}