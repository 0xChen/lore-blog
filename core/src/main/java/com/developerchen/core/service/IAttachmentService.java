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
public interface IAttachmentService extends IBaseService<Attachment> {
    /**
     * 获取指定类型附件的数量, 如果没有指定类型则获取所有附件数量
     *
     * @param type 附件类型
     * @return int 附件数量
     */
    Long countAttachment(String type);

    /**
     * 保存附件到本地磁盘, 并在数据库中记录附件信息
     *
     * @param file       待保存附件
     * @param attachment 附件参数
     * @return 保存后的文件数据包括文件路径等
     */
    Attachment saveAttachment(MultipartFile file, Attachment attachment);

    /**
     * 加载指定附件
     *
     * @param attachmentName 附件名
     * @return Resource资源形式的附件
     */
    Resource loadAttachmentAsResource(String attachmentName);

    /**
     * 删除所有通过系统上传功能保存的附件,
     * 包括磁盘文件及数据库的附件记录
     */
    void deleteAllAttachment();

    /**
     * 批量删除附件,
     * 包括磁盘中的文件及对应的数据库附件记录
     *
     * @param attachmentIds 待删除附件ID集合
     */
    void deleteAttachment(Set<Long> attachmentIds);

    /**
     * 获取分页形式附件信息
     *
     * @param name         附件名
     * @param originalName 原始文件名
     * @param key          附件的唯一标识
     * @param type         附件类型
     * @param description  附件描述
     * @param page         当前页码
     * @param size         每页数量
     * @return 带有分页信息的附件
     */
    IPage<Attachment> getAttachments(String name,
                                     String originalName,
                                     String key,
                                     String type,
                                     String description,
                                     long page, long size);

    /**
     * 根据附件名加载上传目录下的单个文件
     *
     * @param attachmentName 附件名
     * @return Path
     */
    Path load(String attachmentName);

    /**
     * 加载上传目录下的所有文件
     *
     * @return Stream<Path>
     */
    Stream<Path> loadAll();

}
