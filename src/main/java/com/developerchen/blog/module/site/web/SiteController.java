package com.developerchen.blog.module.site.web;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.site.domain.Sitemap;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.web.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 站点
 *
 * @author syc
 */
@Controller
public class SiteController extends BaseController {

    private final ISiteService siteService;

    public SiteController(ISiteService siteService) {
        this.siteService = siteService;
    }


    /**
     * 首页
     */
    @GetMapping(value = {"", "/", "/index"})
    public String index(@RequestParam(defaultValue = "1") Long page,
                        @RequestParam(required = false) Long size, Model model) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "themes/{theme}/index";
    }

    @ResponseBody()
    @GetMapping(value = "sitemap.xml", produces = {"application/xml; charset=utf-8"})
    public String sitemap() {
        List<Post> postList = siteService.getPostForSitemap();
        Sitemap sitemap = new Sitemap(postList.size());
        for (Post post : postList) {
            String postUrl = AppConfig.scheme + "://" + AppConfig.hostname + "/post/";
            if (StringUtils.isNotEmpty(post.getSlug())) {
                postUrl = postUrl + post.getSlug();
            } else {
                postUrl = postUrl + post.getId();
            }

            Date lastMod = post.getUpdateTime();
            lastMod = lastMod == null ? post.getCreateTime() : lastMod;
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(lastMod.toInstant(),
                    ZoneOffset.systemDefault());
            sitemap.addUrl(postUrl, offsetDateTime.withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        return sitemap.toXmlString();
    }

    /**
     * 安装页面
     */
    @GetMapping(BlogConst.INSTALL_URI)
    public String install() {
        return "install";
    }

    /**
     * 保存安装数据
     */
    @ResponseBody
    @PostMapping(BlogConst.INSTALL_URI)
    public RestResponse<String> install(@RequestBody Map<String, String> parameterMap) {
        // 保存初始化数据
        this.siteService.install(parameterMap);

        // 生成锁定文件
        String errorMessage = null;
        try {
            File parent = BlogConst.INSTALLED.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException(parent.getPath() + "目录创建失败. ");
                }
            }
            String content = "Lore-Blog安装后自动生成的文件, 用于锁定重复安装. 删除此文件才可以重新执行安装程序. ";
            Files.writeString(BlogConst.INSTALLED.toPath(), content);
        } catch (IOException e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (errorMessage != null) {
            BlogConst.HAS_INSTALLED = false;
            return RestResponse.fail("初始化站点失败, 无法创建[" +
                    BlogConst.INSTALLED.getPath() + "]文件, " + errorMessage + ". ");
        } else {
            BlogConst.HAS_INSTALLED = true;
            return RestResponse.ok();
        }
    }

}
