package com.developerchen.blog.module.site.web.api;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.site.domain.Sitemap;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.constant.Const;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.system.config.SystemConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
@RestController
public class SiteController extends BaseController {

    private final ISiteService siteService;

    public SiteController(ISiteService siteService) {
        this.siteService = siteService;
    }


    @GetMapping(value = "sitemap.xml", produces = {"application/xml; charset=utf-8"})
    public String sitemap() {
        List<Post> postList = siteService.getPostForSitemap();
        Sitemap sitemap = new Sitemap(postList.size());
        for (Post post : postList) {
            String postUrl = SystemConfig.scheme + "://" + SystemConfig.hostname + "/post/";
            if (StringUtils.isNotEmpty(post.getSlug())) {
                postUrl = postUrl + post.getSlug();
            } else {
                postUrl = postUrl + post.getId();
            }

            LocalDateTime lastMod = post.getUpdateTime();
            lastMod = lastMod == null ? post.getCreateTime() : lastMod;
            sitemap.addUrl(postUrl, lastMod.withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        return sitemap.toXmlString();
    }


    /**
     * 保存安装数据
     */
    @PostMapping(BlogConst.INSTALL_URI)
    public R<String> install(@RequestBody Map<String, String> parameterMap) {
        // 保存初始化数据
        this.siteService.install(parameterMap);

        // 生成锁定文件
        String errorMessage = null;
        try {
            File parent = Const.INSTALLED.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException(parent.getPath() + "目录创建失败. ");
                }
            }
            String content = "Lore-Blog安装后自动生成的文件, 用于锁定重复安装. 删除此文件才可以重新执行安装程序. ";
            Files.writeString(Const.INSTALLED.toPath(), content);
        } catch (IOException e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (errorMessage != null) {
            BlogConst.HAS_INSTALLED = false;
            return R.fail("初始化站点失败, 无法创建[" +
                    Const.INSTALLED.getPath() + "]文件, " + errorMessage + ". ");
        } else {
            BlogConst.HAS_INSTALLED = true;
            return R.ok();
        }
    }

}
