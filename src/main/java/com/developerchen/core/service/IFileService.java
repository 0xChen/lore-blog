package com.developerchen.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.core.domain.entity.Attachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <p>
 * 文件、图片等 服务类
 * </p>
 *
 * @author syc
 */
public interface IFileService extends IBaseService<Attachment> {
    int fileCount(String type);

    String saveFile(MultipartFile files);

    Resource loadAsResource(String filename);

    void deleteAll();

    void delete(Set<Long> fileIds);

    IPage<Attachment> getFiles(String name, String key, String type, long page, long size);

    Path load(String filename);

    Stream<Path> loadAll();


}
