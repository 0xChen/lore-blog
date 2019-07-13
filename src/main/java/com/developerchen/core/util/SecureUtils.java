package com.developerchen.core.util;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.exception.CryptoException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 安全相关工具类, 加密解密等
 *
 * @author syc
 */
public final class SecureUtils {

    /**
     * Default initialization vector which use IVs are ciphers in feedback mode
     */
    private static final IvParameterSpec IV_PARAMETER_SPEC =
            new IvParameterSpec("I-love-lore-blog".getBytes(StandardCharsets.UTF_8));

    /**
     * Cipher instant cache
     */
    private static final Map<String, Cipher> CIPHER_CACHE = new HashMap<>();


    /**
     * 获取一个 Cipher 实例
     *
     * @param transformation transformation的名称, 例如: AES/CBC/PKCS5Padding
     */
    public static Cipher getCipherInstant(String transformation) {
        Cipher cipher;
        if (SecureUtils.CIPHER_CACHE.containsKey(transformation)) {
            cipher = SecureUtils.CIPHER_CACHE.get(transformation);
        } else {
            try {
                cipher = Cipher.getInstance(transformation);
                SecureUtils.CIPHER_CACHE.put(transformation, cipher);
            } catch (Exception e) {
                throw new CryptoException(e);
            }
        }
        return cipher;
    }


    /**
     * 使用默认的配置方式加密数据
     * transformation: "AES/CBC/PKCS5Padding"
     * AlgorithmParameterSpec: IV_PARAMETER_SPEC
     *
     * @param data      要加密的原始字符串
     * @param secretKey 用来加密的密钥
     * @return 加密后的字符串的Base64表现形式
     */
    public static String encrypt(String data, String secretKey) {
        Cipher cipher = getCipherInstant("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IV_PARAMETER_SPEC);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] base64Data = Base64.getEncoder().encode(encryptedData);
            return new String(base64Data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 使用默认的配置方式 解密数据
     * transformation: "AES/CBC/PKCS5Padding"
     * AlgorithmParameterSpec: IV_PARAMETER_SPEC
     *
     * @param encryptedData 要解密的原始base64字符串
     * @param secretKey     用来解密的密钥
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedData, String secretKey) {
        Cipher cipher = getCipherInstant("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IV_PARAMETER_SPEC);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 生成用于加密jwt token的密钥文件
     *
     * @param secretKey 密钥, 如果是空则生成随机密钥
     * @throws IOException
     */
    public static void generateJwtSecretKeyFile(String secretKey) throws IOException {
        generateSecretKeyFile("classpath:jwt.properties", secretKey, "jwtSecretKey");
    }

    /**
     * 生成用于数据库连接密码的密钥文件
     *
     * @param secretKey 密钥, 如果是空则生成随机密钥
     * @throws IOException
     */
    public static void generateJdbcSecretKeyFile(String secretKey) throws IOException {
        generateSecretKeyFile("classpath:jdbc.properties", secretKey, "jdbcSecretKey");
    }

    /**
     * 生成用于加密解密的密钥文件
     *
     * @param propertyPath 配置文件的位置, 配置文件中"secretKeyPath"的值决定了secretKey文件生成的位置
     * @param secretKey    密钥, 如果是空则生成随机密钥
     * @param fileName     生成的密钥的文件名
     * @throws IOException
     */
    public static void generateSecretKeyFile(String propertyPath,
                                             String secretKey,
                                             String fileName) throws IOException {
        File file = ResourceUtils.getFile(propertyPath);
        Properties properties = new Properties();
        properties.load(new FileReader(file));
        String secretKeyPath = properties.getProperty("secretKeyPath");
        if (StringUtils.isBlank(secretKeyPath)) {
            // 如果没有指定位置则在当前项目所在目录下生成
            secretKeyPath = AppConfig.HOME_PATH + File.separator + fileName;
        }
        File secretKeyFile = new File(secretKeyPath);
        if (StringUtils.isEmpty(secretKey)) {
            secretKey = RandomStringUtils.randomPrint(32);
        }
        try (FileWriter fileWriter = new FileWriter(secretKeyFile)) {
            fileWriter.write(secretKey);
            fileWriter.flush();
        }
        if (!secretKeyFile.exists()) {
            boolean success = secretKeyFile.createNewFile();
            if (!success) {
                throw new IOException(fileName + "文件创建失败.");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        generateJwtSecretKeyFile(null);
        System.out.println("jwtSecretKey文件成功生成!");
        generateJdbcSecretKeyFile(null);
        System.out.println("jdbcSecretKey文件成功生成!");
    }

}
