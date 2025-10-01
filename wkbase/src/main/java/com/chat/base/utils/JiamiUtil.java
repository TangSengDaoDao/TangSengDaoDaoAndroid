package com.chat.base.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class JiamiUtil {
    
    /**
     * 默认私钥
     */
    private static final String DEFAULT_PRIVATE_KEY = "saiyue";
    
    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";
    
    /**
     * 加密模式
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 使用默认私钥"saiyue"加密字符串
     * 
     * @param plainText 待加密的字符串
     * @return 加密后的Base64编码字符串
     * @throws Exception 加密异常
     */
    public static String encrypt(String plainText) throws Exception {
        return encrypt(plainText, DEFAULT_PRIVATE_KEY);
    }
    
    /**
     * 使用指定私钥加密字符串
     * 
     * @param plainText 待加密的字符串
     * @param privateKey 私钥
     * @return 加密后的Base64编码字符串
     * @throws Exception 加密异常
     */
    public static String encrypt(String plainText, String privateKey) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("待加密字符串不能为空");
        }
        
        // 生成密钥
        SecretKeySpec secretKey = generateKey(privateKey);
        
        // 创建加密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        // 执行加密
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // 返回Base64编码的加密结果
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }
    
    /**
     * 使用默认私钥"saiyue"解密字符串
     * 
     * @param encryptedText 待解密的Base64编码字符串
     * @return 解密后的原始字符串
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        return decrypt(encryptedText, DEFAULT_PRIVATE_KEY);
    }
    
    /**
     * 使用指定私钥解密字符串
     * 
     * @param encryptedText 待解密的Base64编码字符串
     * @param privateKey 私钥
     * @return 解密后的原始字符串
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedText, String privateKey) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("待解密字符串不能为空");
        }
        
        // 生成密钥
        SecretKeySpec secretKey = generateKey(privateKey);
        
        // 创建解密器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        // 解码Base64并执行解密
        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        // 返回解密后的字符串
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 生成AES密钥
     * 
     * @param privateKey 私钥字符串
     * @return SecretKeySpec对象
     * @throws NoSuchAlgorithmException 算法异常
     */
    private static SecretKeySpec generateKey(String privateKey) throws NoSuchAlgorithmException {
        // 使用MD5哈希确保密钥长度为16字节（128位）
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyBytes = md.digest(privateKey.getBytes(StandardCharsets.UTF_8));
        
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * 生成随机AES密钥
     * 
     * @return 随机生成的Base64编码密钥
     * @throws NoSuchAlgorithmException 算法异常
     */
    public static String generateRandomKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128); // 128位密钥
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
    }
    
    /**
     * 验证字符串是否可以被正确加密和解密
     * 
     * @param testString 测试字符串
     * @return 验证结果
     */
    public static boolean validateEncryption(String testString) {
        try {
            String encrypted = encrypt(testString);
            String decrypted = decrypt(encrypted);
            return testString.equals(decrypted);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取默认私钥
     * 
     * @return 默认私钥
     */
    public static String getDefaultPrivateKey() {
        return DEFAULT_PRIVATE_KEY;
    }
    
    /**
     * 测试方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 测试字符串
            String testString = "https://apijw.newhxchat.top";
            
            System.out.println("原始字符串: " + testString);
            System.out.println("使用私钥: " + DEFAULT_PRIVATE_KEY);
            
            // 加密
            String encrypted = encrypt(testString);
            System.out.println("加密后: " + encrypted);
            
            // 解密
            String decrypted = decrypt(encrypted);
            System.out.println("解密后: " + decrypted);
            
            // 验证
            boolean isValid = validateEncryption(testString);
            System.out.println("加密解密验证: " + (isValid ? "成功" : "失败"));
            
            // 测试自定义私钥
            String customKey = "myCustomKey";
            String encryptedWithCustomKey = encrypt(testString, customKey);
            String decryptedWithCustomKey = decrypt(encryptedWithCustomKey, customKey);
            System.out.println("\n使用自定义私钥测试:");
            System.out.println("自定义私钥: " + customKey);
            System.out.println("加密后: " + encryptedWithCustomKey);
            System.out.println("解密后: " + decryptedWithCustomKey);
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}