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
     * hostName
     */
    private String hostname;


    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
