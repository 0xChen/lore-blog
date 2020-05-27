package com.developerchen.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.entity.Attachment;
import com.developerchen.core.exception.TipException;
import com.developerchen.core.repository.FileMapper;
import com.developerchen.core.service.IFileService;
import com.developerchen.core.util.FileUtils;
import com.developerchen.core.domain.RestPage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
public class FileServiceImpl extends BaseServiceImpl<FileMapper, Attachment> implements IFileService {

    private final Path filePath;

    public FileServiceImpl() {
        this.filePath = Paths.get(AppConfig.fileLocation);
    }


    /**
     * 获取指定类型文件的数量, 如果没有指定类型则获取所有文件数量
     *
     * @return int 文件数量
     */
    @Override
    public int fileCount(String type) {
        QueryWrapper<Attachment> qw = new QueryWrapper<>();
        qw.eq(type != null, "type", type);
        return baseMapper.selectCount(qw);
    }

    /**
     * 保存文件到本地磁盘, 并在数据库中记录文件信息
     *
     * @param file 待保存文件
     * @return 保存后的文件路径
     */
    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        try {
            if (originalFilename == null) {
                throw new TipException("File upload error");
            }
            originalFilename = StringUtils.cleanPath(originalFilename);
            if (file.isEmpty()) {
                throw new TipException("Failed to store empty file " + originalFilename);
            }
            if (originalFilename.contains(FileUtils.TOP_PATH)) {
                // security check
                throw new TipException("Cannot store file with relative path outside current directory "
                        + originalFilename);
            }
            // 存储文件基础信息到数据库
            Long size = file.getSize();
            String type = file.getContentType();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 避免文件名称重复，生成新的具有唯一性的文件名
            String newFilename = IdWorker.getId() + suffix;

            Attachment attachment = new Attachment();
            attachment.setName(newFilename);
            attachment.setOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setType(type);
            baseMapper.insert(attachment);
            // 以新文件名保存文件到磁盘
            try (InputStream inputStream = file.getInputStream()) {
                Path path = this.filePath.resolve(newFilename);
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return newFilename;
        } catch (Exception e) {
            throw new TipException("Failed to store file " + originalFilename, e);
        }
    }

    /**
     * 加载指定文件
     *
     * @param filename 文件名
     * @return Resource资源形式的文件
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new TipException("读取文件失败: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new TipException("读取文件失败: " + filename, e);
        }
    }

    /**
     * 删除所有通过系统上传功能保存的文件,
     * 包括磁盘文件及数据库的文件记录
     */
    @Override
    public void deleteAll() {
        // 清空附件表数据
        baseMapper.delete(null);
        // 清空磁盘文件
        if (!FileSystemUtils.deleteRecursively(filePath.toFile())) {
            throw new TipException("删除文件失败");
        }
    }

    /**
     * 批量删除指定文件,
     * 包括磁盘中的文件及对应的数据库文件记录
     *
     * @param fileIds 待删除文件ID集合
     */
    @Override
    public void delete(Set<Long> fileIds) {
        List<Attachment> attachmentList = baseMapper.selectBatchIds(fileIds);
        // 删除数据库数据
        baseMapper.deleteBatchIds(fileIds);
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
            throw new TipException("以下文件删除失败: " + deleteFailedFilenameSet.toString()
                    + ". 以下文件从磁盘成功删除, 但是数据库记录没有删除, 请重新删除: " + deleteSuccessFilenameSet.toString());
        }
    }

    /**
     * 获取分页形式附件信息
     *
     * @param name 附件名
     * @param key  附件的唯一标识
     * @param type 附件类型
     * @param page 当前页码
     * @param size 每页数量
     * @return 带有分页信息的附件
     */
    @Override
    public IPage<Attachment> getFiles(String name,
                                      String key,
                                      String type,
                                      long page,
                                      long size) {
        QueryWrapper<Attachment> qw = new QueryWrapper<>();
        qw.like(StringUtils.hasText(name), "original_name", name);
        qw.eq(StringUtils.hasText(key), "key", key);
        qw.like(StringUtils.hasText(type), "type", type);
        qw.orderByDesc("create_time");
        qw.orderByAsc("original_name");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

    /**
     * 根据文件名加载上传目录下的单个文件
     *
     * @param filename 文件名
     */
    @Override
    public Path load(String filename) {
        return filePath.resolve(filename);
    }

    /**
     * 加载上传目录下的所有文件
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.filePath, 1)
                    .filter(path -> !path.equals(this.filePath))
                    .map(this.filePath::relativize);
        } catch (IOException e) {
            throw new TipException("读取文件失败", e);
        }
    }
}
