package com.developerchen.core.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
        return getFiles(location)[0];
    }

    /**
     * 获取指定位置下的所有文件, 如果文件是Jar文件中的文件
     * 将读取文件中的内容并写入一个同名的临时文件中然后返回这个临时文件
     *
     * @param locationPattern 文件位置, 可使用通配符等
     * @return 一组文件
     */
    public static File[] getFiles(String locationPattern) throws IOException {
        Resource[] resources = RESOURCE_RESOLVER.getResources(locationPattern);
        File[] files = new File[resources.length];
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            if (!resource.exists()) {
                continue;
            }
            if (resource.isFile()) {
                files[i] = resource.getFile();
            } else {
                // 可能是Jar中的文件尝试以流的形式获取
                String filename = resource.getFilename();
                if (filename != null) {
                    String suffix = filename.substring(filename.lastIndexOf("."));
                    String prefix = filename.replace(suffix, "");
                    Path path = Files.createTempFile(prefix, suffix);
                    try (InputStream inputStream = resource.getInputStream()) {
                        byte[] bytes = new byte[inputStream.available()];
                        int read = inputStream.read(bytes);
                        if (bytes.length != read) {
                            inputStream.close();
                            throw new IOException("读取文件内容时发生错误.");
                        }
                        Files.write(path, bytes);
                        files[i] = path.toFile();
                    }
                }
            }
        }
        return files;
    }

    /**
     * {@link org.springframework.core.io.support.ResourcePatternResolver#getResource(String)}
     */
    public static Resource getResource(String location) throws IOException {
        return RESOURCE_RESOLVER.getResource(location);
    }

    /**
     * {@link org.springframework.core.io.support.ResourcePatternResolver#getResources(String)}
     */
    public static Resource[] getResources(String locationPattern) throws IOException {
        return RESOURCE_RESOLVER.getResources(locationPattern);
    }

    /**
     * 创建文件并且连同父目录一起创建
     *
     * @param locationPattern 文件位置, 可使用通配符等
     * @return 创建后的文件
     */
    public static File createFile(String locationPattern) throws IOException {
        Resource resource = RESOURCE_RESOLVER.getResource(locationPattern);
        // 如果resource文件不存在则创建并且连同父目录一起创建
        if (!resource.exists()) {
            File file = resource.getFile();
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                // 如果创建失败则抛出异常
                if (!parentFile.mkdirs()) {
                    throw new IOException("创建文件夹失败: " + parentFile.getAbsolutePath());
                }
            }
            // 如果创建失败则抛出异常
            if (!file.createNewFile()) {
                throw new IOException("创建文件失败: " + file.getAbsolutePath());
            }
            return file;
        }
        return resource.getFile();
    }
}
