package com.developerchen.core.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

/**
 * 文件工具类
 *
 * @author syc
 */
public class FileUtils {

    private static final PathMatchingResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();

    private static final String FOLDER_SEPARATOR = File.separator;

    public static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";

    private static final char EXTENSION_SEPARATOR = '.';

    private FileUtils() {
    }

    /**
     * Extract the filename extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" -> "txt".
     *
     * @param path the file path (may be {@code null})
     * @return the extracted filename extension, or {@code null} if none
     */
    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (folderIndex > extIndex) {
            return null;
        }

        return path.substring(extIndex + 1);
    }

    /**
     * 获取文件名(不包含后缀)
     *
     * @param path 文件路径
     * @return 文件名
     */
    public static String getFilenamePrefix(String path) {
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return path;
        }
        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        return path.substring(folderIndex + 1, path.lastIndexOf("."));
    }

    /**
     * {@link org.springframework.core.io.support.ResourcePatternResolver#getResource(String)}
     *
     * @param location 文件位置
     * @return 文件
     */
    public static File getFile(String location) throws IOException {
        return RESOURCE_RESOLVER.getResource(location).getFile();
    }

    /**
     * {@link org.springframework.core.io.support.ResourcePatternResolver#getResources(String)}
     */
    public static File[] getResources(String locationPattern) throws IOException {
        Resource[] resources = RESOURCE_RESOLVER.getResources(locationPattern);
        File[] files = new File[resources.length];
        for (int i = 0; i < resources.length; i++) {
            files[i] = resources[i].getFile();
        }
        return files;
    }

}
