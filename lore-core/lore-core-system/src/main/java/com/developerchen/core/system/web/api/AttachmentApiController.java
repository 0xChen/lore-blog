package com.developerchen.core.system.web.api;

import com.developerchen.core.base.BaseController;
import com.developerchen.core.system.service.IAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 附件(文件、图片等) 前端控制器
 *
 * @author syc
 */
@RestController
public class AttachmentApiController extends BaseController {

    private final IAttachmentService attachmentService;

    public AttachmentApiController(IAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }


    /**
     * 显示指定文件名的图片或者下载指定文件
     *
     * @param filename 文件名
     */
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = attachmentService.loadAttachmentAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
