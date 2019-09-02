package com.developerchen.core.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.Attachment;
import com.developerchen.core.service.IFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 附件(文件、图片等) 前端控制器
 *
 * @author syc
 */
@Controller
public class FileController extends BaseController {

    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 保存上传的文件到本地磁盘, 并在数据库中记录文件信息
     *
     * @param file 文件
     * @return 保存后的文件路径
     */
    @ResponseBody
    @PostMapping("/file")
    public RestResponse upload(@RequestParam("file") MultipartFile file) {
        String filePath = fileService.saveFile(file);
        return RestResponse.ok(filePath);
    }

    /**
     * 保存上传的一组文件到本地磁盘, 并在数据库中记录文件信息
     *
     * @param fileMap 一组文件
     * @return 保存后的文件路径
     */
    @ResponseBody
    @PostMapping("/files")
    public RestResponse upload(@RequestParam LinkedHashMap<String, MultipartFile> fileMap) {
        List<String> filePathList = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            String filePath = fileService.saveFile(entry.getValue());
            filePathList.add(filePath);
        }
        return RestResponse.ok(filePathList);
    }

    /**
     * 显示指定文件名的图片或者下载指定文件
     *
     * @param filename 文件名
     */
    @ResponseBody
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = fileService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    /**
     * 删除所有通过系统上传功能保存的文件,
     * 包括磁盘文件及数据库的文件记录
     */
    @ResponseBody
    @DeleteMapping("/file")
    public RestResponse deleteAll() {
        fileService.deleteAll();
        return RestResponse.ok("删除成功！");
    }

    /**
     * 批量删除指定文件,
     * 包括磁盘中的文件及对应的数据库文件记录
     *
     * @param fileIds 待删除文件ID集合
     */
    @ResponseBody
    @DeleteMapping("/file/{fileIds}")
    public RestResponse delete(@PathVariable Set<Long> fileIds) {
        fileService.delete(fileIds);
        return RestResponse.ok("删除成功！");
    }

    /**
     * 分页形式获取文件列表
     *
     * @param name 文件名搜索条件
     * @param key  文件Key搜索条件
     * @param type 文件类型搜索条件
     * @param page 当前页码
     * @param size 每页显示数量
     * @return 分页形式的文件列表
     */
    @ResponseBody
    @GetMapping("/files")
    public RestResponse page(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String key,
                             @RequestParam(required = false) String type,
                             @RequestParam(defaultValue = "1") long page,
                             @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Attachment> filePage = fileService.getFiles(name, key, type, page, size);
        return RestResponse.ok(filePage);
    }
}
