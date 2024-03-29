package com.developerchen.core.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.Attachment;
import com.developerchen.core.service.IAttachmentService;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 附件(文件、图片等) 前端控制器
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class AttachmentAdminController extends BaseController {

    private final IAttachmentService attachmentService;

    public AttachmentAdminController(IAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * 附件总数量
     *
     * @param type 要统计的附件类型, 没有就统计所有
     * @return 总数量
     */
    @ResponseBody
    @GetMapping("/attachment/count")
    public RestResponse<Long> count(@RequestParam(value = "type", required = false) String type) {
        Long total = attachmentService.countAttachment(type);
        return RestResponse.ok(total);
    }

    /**
     * 保存上传的附件到本地磁盘, 并在数据库中记录附件信息
     *
     * @param multipartFile 附件
     * @return 保存后的附件数据
     */
    @ResponseBody
    @PostMapping("/attachments")
    public RestResponse<Attachment> upload(@RequestParam("file") MultipartFile multipartFile,
                                           Attachment attachment) {
        Attachment returnAttachment = attachmentService.saveAttachment(multipartFile, attachment);
        return RestResponse.ok(returnAttachment);
    }

    /**
     * 保存上传的一组附件到本地磁盘, 并在数据库中记录附件信息
     * 使用 MultiValueMap 所以前端上传文件时可以一个 key 对应多个文件
     *
     * @param multipartFileMap 附件
     * @return 保存后的附件数据
     */
    @ResponseBody
    @PostMapping("/attachment/batch")
    public RestResponse<List<Attachment>> uploadBatch(
            @RequestParam MultiValueMap<String, MultipartFile> multipartFileMap) {
        List<Attachment> attachmentList = multipartFileMap.values().stream()
                .flatMap(fileList -> fileList.stream()
                        .map(file -> attachmentService.saveAttachment(file, new Attachment())))
                .collect(Collectors.toList());
        return RestResponse.ok(attachmentList);
    }

    /**
     * 删除所有通过系统上传功能保存的附件,
     * 包括磁盘文件及数据库的附件记录
     */
    @ResponseBody
    @DeleteMapping("/attachments")
    public RestResponse<?> deleteAll() {
        attachmentService.deleteAllAttachment();
        return RestResponse.ok("删除成功！");
    }

    /**
     * 删除指定附件,
     * 包括磁盘中的文件及对应的数据库附件记录
     *
     * @param attachmentId 待删除附件ID
     */
    @ResponseBody
    @DeleteMapping("/attachments/{attachmentId}")
    public RestResponse<?> delete(@PathVariable long attachmentId) {
        Set<Long> attachmentIdSet = new HashSet<>();
        attachmentIdSet.add(attachmentId);

        attachmentService.deleteAttachment(attachmentIdSet);
        return RestResponse.ok("删除成功！");
    }

    /**
     * 批量删除指定附件,
     * 包括磁盘中的文件及对应的数据库附件记录
     *
     * @param attachmentIds 待删除附件ID集合
     */
    @ResponseBody
    @DeleteMapping("/attachments/{attachmentIds}/batch")
    public RestResponse<?> deleteBatch(@PathVariable Set<Long> attachmentIds) {
        attachmentService.deleteAttachment(attachmentIds);
        return RestResponse.ok("删除成功！");
    }

    /**
     * 分页形式获取附件列表
     *
     * @param name 附件名搜索条件
     * @param key  附件Key搜索条件
     * @param type 附件类型搜索条件
     * @param page 当前页码
     * @param size 每页显示数量
     * @return 分页形式的附件列表
     */
    @ResponseBody
    @GetMapping("/attachments")
    public RestResponse<IPage<Attachment>> page(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String originalName,
                                                @RequestParam(required = false) String key,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) String description,
                                                @RequestParam(defaultValue = "1") long page,
                                                @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Attachment> attachmentPage = attachmentService.getAttachments(
                name, originalName, key, type, description, page, size);
        return RestResponse.ok(attachmentPage);
    }
}
