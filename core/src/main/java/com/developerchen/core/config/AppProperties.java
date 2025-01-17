package com.developerchen.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for application.
 *
 * @author syc
 */
@ConfigurationProperties(prefix = "my-app")
public class AppProperties {

    /**
     * Folder location for storing files
     */
    private String fileLocation;

    /**
     * Path pattern used for static resources.
     */
    private String staticPathPattern;

    /**
     * scheme
     */
    private String scheme;

    /**
     * hostName
     */
    private String hostname;

    /**
     * JWT token的有效期(毫秒), 用于计算JWT的过期时间.
     */
    private Long jwtExpireTime;

    /**
     * 用于加密解密JWT token的密钥文件的位置, 如果不指定位置则程序会在当前项目所在
     * 目录下查找文件名为 "jwtSecretKey" 的文件. 如果仍然获取不到, 则生成随机密钥.
     * 文件内容应为256 bits (32 bytes)长度的文本. 例: WoQu@*Nian~Mai-Le%#GeDa^Jin#Biao
     */
    private String jwtSecretKeyPath;

    /**
     * 用于加密解密生产环境数据库连接密码的密钥文件的位置
     */
    private String jdbcSecretKeyPath;

    
    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getStaticPathPattern() {
        return staticPathPattern;
    }

    public void setStaticPathPattern(String staticPathPattern) {
        this.staticPathPattern = staticPathPattern;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setJwtExpireTime(Long jwtExpireTime) {
        this.jwtExpireTime = jwtExpireTime;
    }

    public Long getJwtExpireTime() {
        return jwtExpireTime;
    }

    public String getJwtSecretKeyPath() {
        return jwtSecretKeyPath;
    }

    public void setJwtSecretKeyPath(String jwtSecretKeyPath) {
        this.jwtSecretKeyPath = jwtSecretKeyPath;
    }

    public String getJdbcSecretKeyPath() {
        return jdbcSecretKeyPath;
    }

    public void setJdbcSecretKeyPath(String jdbcSecretKeyPath) {
        this.jdbcSecretKeyPath = jdbcSecretKeyPath;
    }
}
