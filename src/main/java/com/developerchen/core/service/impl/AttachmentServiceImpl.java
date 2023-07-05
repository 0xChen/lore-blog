package com.developerchen.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.RestPage;
import com.developerchen.core.domain.entity.Attachment;
import com.developerchen.core.exception.AlertException;
import com.developerchen.core.repository.AttachmentMapper;
import com.developerchen.core.service.IAttachmentService;
import com.developerchen.core.util.FileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>
 * 附件(文件、图片等) 服务实现类
 * </p>
 *
 * @author syc
 */

@Service
public class AttachmentServiceImpl extends BaseServiceImpl<AttachmentMapper, Attachment>
        implements IAttachmentService {

    private final Path filePath;

    public AttachmentServiceImpl() {
        this.filePath = Paths.get(AppConfig.fileLocation);
    }


    @Override
    public Long countAttachment(String type) {
        QueryWrapper<Attachment> qw = new QueryWrapper<>();
        qw.eq(type != null, "type", type);
        return baseMapper.selectCount(qw);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public Attachment saveAttachment(MultipartFile file, Attachment attachment) {
        String originalFilename = file.getOriginalFilename();
        try {
            if (originalFilename == null) {
                throw new AlertException("File upload error");
            }
            originalFilename = StringUtils.cleanPath(originalFilename);
            if (file.isEmpty()) {
                throw new AlertException("Failed to store empty file " + originalFilename);
            }
            if (originalFilename.contains(FileUtils.TOP_PATH)) {
                // security check
                throw new AlertException("Cannot store file with relative path outside current directory "
                        + originalFilename);
            }
            // 存储文件基础信息到数据库
            if (attachment.getWidth() == null || attachment.getHeight() == null) {
                try (InputStream inputStream = file.getInputStream()) {
                    try {
                        BufferedImage image = ImageIO.read(inputStream);
                        int width = image.getWidth();
                        int height = image.getHeight();
                        if (width > 0 && height > 0) {
                            attachment.setWidth(width);
                            attachment.setHeight(height);
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            Long size = file.getSize();
            String sha1 = DigestUtils.sha1Hex(file.getBytes());
            String type = file.getContentType();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 避免文件名称重复，生成新的具有唯一性的文件名
            String newFilename = IdWorker.getId() + suffix;

            attachment.setName(newFilename);
            attachment.setOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setSha1(sha1);
            attachment.setType(type);
            baseMapper.insert(attachment);
            // 以新文件名保存文件到磁盘
            try (InputStream inputStream = file.getInputStream()) {
                Path path = this.filePath.resolve(newFilename);
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return attachment;
        } catch (Exception e) {
            throw new AlertException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadAttachmentAsResource(String attachmentName) {
        try {
            Path file = load(attachmentName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new AlertException("读取文件失败: " + attachmentName);

            }
        } catch (MalformedURLException e) {
            throw new AlertException("读取文件失败: " + attachmentName, e);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteAllAttachment() {
        // 清空附件表数据
        baseMapper.delete(null);
        // 清空磁盘文件
        if (!FileSystemUtils.deleteRecursively(filePath.toFile())) {
            throw new AlertException("删除文件失败");
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteAttachment(Set<Long> attachmentIds) {
        List<Attachment> attachmentList = baseMapper.selectBatchIds(attachmentIds);
        // 删除数据库数据
        baseMapper.deleteBatchIds(attachmentIds);
        // 删除磁盘文件
        Set<String> deleteFailedFilenameSet = new HashSet<>();
        Set<String> deleteSuccessFilenameSet = new HashSet<>();
        for (Attachment attachment : attachmentList) {
            String filename = attachment.getName();
            Path path = this.filePath.resolve(filename);
            File file = path.toFile();
            if (file.exists()) {
                if (!file.delete()) {
                    deleteFailedFilenameSet.add(filename);
                } else {
                    deleteSuccessFilenameSet.add(filename);
                }
            }
        }
        if (deleteFailedFilenameSet.size() > 0) {
            throw new AlertException("以下文件删除失败: " + deleteFailedFilenameSet
                    + ". 以下文件从磁盘成功删除, 但是数据库记录没有删除, 请重新删除: " + deleteSuccessFilenameSet.toString());
        }
    }

    @Override
    public IPage<Attachment> getAttachments(String name,
                                            String originalName,
                                            String key,
                                            String type,
                                            String description,
                                            long page, long size) {
        QueryWrapper<Attachment> qw = new QueryWrapper<>();
        qw.like(StringUtils.hasText(name), "name", name);
        qw.like(StringUtils.hasText(originalName), "original_name", originalName);
        qw.eq(StringUtils.hasText(key), "key", key);
        qw.like(StringUtils.hasText(type), "type", type);
        qw.like(StringUtils.hasText(description), "description", description);
        qw.orderByDesc("create_time");
        qw.orderByAsc("original_name");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

    @Override
    public Path load(String attachmentName) {
        return filePath.resolve(attachmentName);
    }

    @Override
    public Stream<Path> loadAll() {
        try (Stream<Path> stream = Files.walk(this.filePath, 1)
                .filter(path -> !path.equals(this.filePath))
                .map(this.filePath::relativize)) {
            return stream;
        } catch (IOException e) {
            throw new AlertException("读取文件失败", e);
        }
    }
}
