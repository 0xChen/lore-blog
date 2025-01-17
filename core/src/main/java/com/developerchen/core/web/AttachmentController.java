package com.developerchen.core.web;

import com.developerchen.core.service.IAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 附件(文件、图片等) 前端控制器
 *
 * @author syc
 */
@Controller
public class AttachmentController extends BaseController {

    private final IAttachmentService attachmentService;

    public AttachmentController(IAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }


    /**
     * 显示指定文件名的图片或者下载指定文件
     *
     * @param filename 文件名
     */
    @ResponseBody
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = attachmentService.loadAttachmentAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
