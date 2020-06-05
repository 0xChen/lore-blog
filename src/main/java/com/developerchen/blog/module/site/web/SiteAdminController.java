package com.developerchen.blog.module.site.web;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.blog.module.site.domain.dto.ThemeDTO;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.blog.theme.Common;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.exception.RestException;
import com.developerchen.core.util.FileUtils;
import com.developerchen.core.web.BaseController;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 站点后台管理
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class SiteAdminController extends BaseController {

    private final ISiteService siteService;
    private final IPostService postService;
    private final ICommentService commentService;

    public SiteAdminController(ISiteService siteService,
                               IPostService postService,
                               ICommentService commentService) {
        this.siteService = siteService;
        this.postService = postService;
        this.commentService = commentService;
    }

    /**
     * 获取最近评论, 随机文章及后台统计数据
     */
    @GetMapping(value = {"", "/index"})
    public String index(Model model) {
        List<Comment> commentList = commentService.recentComments(BlogConst.COMMENT_RECENT_SIZE);
        List<Post> postList = postService.getPostList(BlogConst.POST_RANDOM, BlogConst.POST_RANDOM_SIZE);
        StatisticsDTO statistics = siteService.getStatistics();

        model.addAttribute("commentList", commentList);
        model.addAttribute("postList", postList);
        model.addAttribute("statistics", statistics);
        return "admin/index";
    }

    /**
     * 获取主题的参数设置
     */
    @GetMapping("/theme/setting")
    public String getThemeSetting(Model model) {
        model.addAllAttributes(AppConfig.OPTIONS);
        return "themes/" + Common.blogTheme() + "/setting";
    }

    /**
     * 获取所有主题
     */
    @ResponseBody
    @GetMapping("/themes")
    public RestResponse getThemes() {
        List<ThemeDTO> themeList = new ArrayList<>(6);
        try {
            // 获取所有主题下的首页页面
            Resource[] resources = FileUtils.getResources("classpath:templates/themes/*/index.html");
            for (Resource resource : resources) {
                String indexPagePath = resource.getURL().getPath();

                int beginIndex = indexPagePath.lastIndexOf("themes/") + 7;
                int endIndex = indexPagePath.lastIndexOf("/index.html");
                String themeName = indexPagePath.substring(beginIndex, endIndex);
                ThemeDTO themeDTO = new ThemeDTO(themeName);
                String settingPagePath = "classpath:templates/themes/" + themeName + "/setting.html";
                if (FileUtils.getResource(settingPagePath).exists()) {
                    themeDTO.setHasSetting(true);
                }
                themeList.add(themeDTO);
            }
        } catch (IOException e) {
            throw new RestException("获取主题文件失败", e);
        }
        return RestResponse.ok(themeList);
    }

    /**
     * 激活指定主题
     */
    @ResponseBody
    @PostMapping("/themes/active")
    public RestResponse activeTheme(@RequestBody Map<String, String> parameterMap) {
        String themeName = parameterMap.get("themeName");
        siteService.activeTheme(themeName);
        return RestResponse.ok();
    }

    /**
     * 设置主题参数
     */
    @ResponseBody
    @PostMapping(path = "/theme/setting", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RestResponse saveThemeSetting(@RequestParam Map<String, String> parameterMap) {
        siteService.saveThemeSetting(parameterMap);
        return RestResponse.ok();
    }

    /**
     * 站点参数设置
     */
    @ResponseBody
    @PostMapping(path = "/site/setting", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RestResponse saveSiteSetting(@RequestParam Map<String, String> parameterMap) {
        siteService.saveSiteSetting(parameterMap);
        return RestResponse.ok();
    }

    /**
     * 获取所有配置项
     */
    @ResponseBody
    @GetMapping("/api/options")
    public RestResponse getOptions() {
        return RestResponse.ok(AppConfig.OPTIONS);
    }

}
