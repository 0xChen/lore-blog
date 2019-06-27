package com.developerchen.blog.module.link.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.blog.module.link.service.ILinkService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 链接 后台管理控制器
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin/api")
public class LinkAdminController extends BaseController {

    private final ILinkService linkService;

    public LinkAdminController(ILinkService linkService) {
        this.linkService = linkService;
    }

    /**
     * 新增或更新链接
     *
     * @param link 链接
     */
    @ResponseBody
    @RequestMapping(value = "/link", method = {RequestMethod.POST, RequestMethod.PUT})
    public RestResponse saveOrUpdate(@RequestBody Link link, BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("保存失败！");
        }
        linkService.saveOrUpdateLink(link);
        return RestResponse.ok(link);
    }

    /**
     * 获取链接
     */
    @ResponseBody
    @GetMapping("/link/{linkId}")
    public RestResponse link(@PathVariable long linkId) {
        Link link = linkService.getLinkById(linkId);
        return RestResponse.ok(link);
    }

    /**
     * 删除所有链接
     */
    @ResponseBody
    @DeleteMapping("/links")
    public RestResponse deleteAll() {
        linkService.deleteAll();
        return RestResponse.ok();
    }

    /**
     * 批量获取链接
     *
     * @param linkIds 链接ID集合
     */
    @ResponseBody
    @DeleteMapping("/links/{linkIds}")
    public RestResponse delete(@PathVariable Set<Long> linkIds) {
        linkService.deleteLinkByIds(linkIds);
        return RestResponse.ok();
    }

    /**
     * 获取链接
     *
     * @param name        链接名称查询条件
     * @param url         链接地址查询条件
     * @param visible     是否可见查询条件
     * @param description 描述查询条件
     * @param page        当前页码
     * @param size        每页数量
     * @return 分页形式的链接
     */
    @ResponseBody
    @GetMapping("/links")
    public RestResponse page(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String url,
                             @RequestParam(required = false) String visible,
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "1") Long page,
                             @RequestParam(required = false) Long size) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        IPage<Link> linkPage = linkService.getLinkPage(name, url, visible, description, page, size);
        return RestResponse.ok(linkPage);
    }

}
