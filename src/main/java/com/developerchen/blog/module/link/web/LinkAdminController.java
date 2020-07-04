package com.developerchen.blog.module.link.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.blog.module.link.service.ILinkService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

/**
 * 链接 后台管理控制器
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
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
    @RequestMapping(value = "/links", method = {RequestMethod.POST, RequestMethod.PUT})
    public RestResponse<Link> saveOrUpdate(@Validated @RequestBody Link link, BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("保存失败！");
        }
        linkService.saveOrUpdateLink(link);
        return RestResponse.ok(link);
    }

    /**
     * 更新链接
     *
     * @param link 链接
     */
    @ResponseBody
    @PutMapping("/links/{linkId}")
    public RestResponse<Link> update(@PathVariable long linkId,
                                     @Validated @RequestBody Link link,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("数据错误, 更新失败！");
        }
        link.setId(linkId);
        linkService.updateLink(link);
        return RestResponse.ok(link);
    }

    /**
     * 获取链接
     */
    @ResponseBody
    @GetMapping("/links/{linkId}")
    public RestResponse<Link> link(@PathVariable long linkId) {
        Link link = linkService.getLinkById(linkId);
        return RestResponse.ok(link);
    }

    /**
     * 删除链接
     *
     * @param linkId 链接ID
     */
    @ResponseBody
    @DeleteMapping("/links/{linkId}")
    public RestResponse<?> delete(@PathVariable long linkId) {
        linkService.deleteLinkById(linkId);
        return RestResponse.ok();
    }

    /**
     * 批量删除链接
     *
     * @param linkIds 链接ID集合
     */
    @ResponseBody
    @DeleteMapping("/links/{linkIds}/batch")
    public RestResponse<?> deleteBatch(@PathVariable Set<Long> linkIds) {
        linkService.deleteLinkByIds(linkIds);
        return RestResponse.ok();
    }

    /**
     * 删除所有链接
     */
    @ResponseBody
    @DeleteMapping("/links")
    public RestResponse<?> deleteAll() {
        linkService.deleteAll();
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
    public RestResponse<IPage<Link>> page(@RequestParam(required = false) String name,
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
