package com.developerchen.core.util;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.exception.CryptoException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全相关工具类, 加密解密等.
 * <p>
 * <a href="http://ctf.ssleye.com/caes.html">...</a> <a href="https://tool.lami.fun/jiami/aes">...</a>
 * 可以用以上两个在线工具生成加密密码.
 * 参考设置:
 * AES加密模式: CBC
 * 填充: pkcs5padding
 * 密钥长度: 256位
 * 密钥: ******
 * 偏移量: I-love-lore-blog
 * 输出: base64
 *
 * @author syc
 */
public final class SecurityUtils {

    /**
     * for encoding user passwords
     */
    public static final PasswordEncoder USER_PASSWORD_ENCODER = PasswordEncoderFactories
            .createDelegatingPasswordEncoder();

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
     * Encode the raw password.
     */
    public static String encodeUserPassword(String password) {
        return SecurityUtils.USER_PASSWORD_ENCODER.encode(password);
    }

    /**
     * Verify the encoded password obtained from storage matches the submitted raw
     * password after it too is encoded. Returns true if the passwords match, false if
     * they do not. The stored password itself is never decoded.
     *
     * @param oldPassword     the raw password to encode and match
     * @param encodedPassword the encoded password from storage to compare with
     * @return true if the raw password, after encoding, matches the encoded password from
     * storage
     */
    public static boolean matchesUserPassword(CharSequence oldPassword,
                                              String encodedPassword) {
        return SecurityUtils.USER_PASSWORD_ENCODER.matches(oldPassword, encodedPassword);
    }

    /**
     * 获取一个 Cipher 实例
     *
     * @param transformation transformation的名称, 例如: AES/CBC/PKCS5Padding
     */
    public static Cipher getCipherInstant(String transformation) {
        Cipher cipher;
        if (SecurityUtils.CIPHER_CACHE.containsKey(transformation)) {
            cipher = SecurityUtils.CIPHER_CACHE.get(transformation);
        } else {
            try {
                cipher = Cipher.getInstance(transformation);
                SecurityUtils.CIPHER_CACHE.put(transformation, cipher);
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
     * @throws IOException 密钥文件创建失败时
     */
    public static void generateJwtSecretKeyFile(String secretKeyPath, String secretKey) throws IOException {
        generateSecretKeyFile(secretKeyPath, secretKey, "jwtSecretKey");
    }

    /**
     * 生成用于数据库连接密码的密钥文件
     *
     * @param secretKey 密钥, 如果是空则生成随机密钥
     * @throws IOException 密钥文件创建失败时
     */
    public static void generateJdbcSecretKeyFile(String secretKeyPath, String secretKey)
            throws IOException {
        generateSecretKeyFile(secretKeyPath, secretKey, "jdbcSecretKey");
    }

    /**
     * 生成用于加密解密的密钥文件
     *
     * @param secretKeyPath 密钥文件生成后的保存位置
     * @param secretKey     密钥, 如果是空则生成随机密钥
     * @param fileName      生成的密钥的文件名
     * @throws IOException 密钥文件创建失败时
     */
    public static void generateSecretKeyFile(String secretKeyPath,
                                             String secretKey,
                                             String fileName) throws IOException {

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

}
