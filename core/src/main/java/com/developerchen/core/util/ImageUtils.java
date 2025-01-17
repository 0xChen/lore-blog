package com.developerchen.core.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 图片工具类
 *
 * @author syc
 */
public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * 判断文件是否是图片类型
     *
     * @param imageFile 图片文件
     * @return {@code true} 是图片, {@code false} 不是图片
     */
    public static boolean isImage(File imageFile) {
        if (!imageFile.exists()) {
            return false;
        }
        try {
            Image img = ImageIO.read(imageFile);
            return img != null && img.getWidth(null) > 0 && img.getHeight(null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过文件名判断指定格式是否为图片
     *
     * @param fileName 文件名
     * @return {@code true} 是图片, {@code false} 不是图片
     */
    public static boolean isImage(String fileName) {
        String fileExtension = FileUtils.getFilenameExtension(fileName);
        List<String> imageExtension = Arrays.asList("jpg", "jpeg", "png", "bmp", "gif",
                "tif", "exif", "svg", "heic");
        return fileExtension != null && imageExtension.contains(fileExtension.toLowerCase());
    }
}
