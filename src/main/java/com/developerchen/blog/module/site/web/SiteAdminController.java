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
import com.developerchen.core.domain.entity.Option;
import com.developerchen.core.exception.RestException;
import com.developerchen.core.util.FileUtils;
import com.developerchen.core.util.JsonUtils;
import com.developerchen.core.web.BaseController;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 站点后台管理
 *
 * @author syc
 */
@RestController
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
    @GetMapping("/dashboard")
    public RestResponse<Map<String, Object>> dashboard() {
        List<Comment> commentList = commentService.recentComments(BlogConst.COMMENT_RECENT_SIZE);
        List<Post> postList = postService.getPostList(BlogConst.POST_RECENT, 10);
        Set<String> tagSet = postService.getTags();
        StatisticsDTO statistics = siteService.getStatistics();

        Map<String, Object> resMap = new HashMap<>(16);
        resMap.put("commentList", commentList);
        resMap.put("postList", postList);
        resMap.put("tagSet", tagSet);
        resMap.put("statistics", statistics);
        return RestResponse.ok(resMap);
    }

    /**
     * 获取所有主题
     */
    @GetMapping("/themes")
    public RestResponse<List<ThemeDTO>> getThemes() {
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
                String settingJsonPath = "classpath:templates/themes/" + themeName + "/setting.json";
                if (FileUtils.getResource(settingJsonPath).exists()) {
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
     * 获取主题的参数设置
     */
    @GetMapping("/themes/{themeName}/setting")
    public RestResponse<List<Option>> getThemeSetting(@PathVariable("themeName") String themeName)
            throws IOException {
        String settingJsonPath = "classpath:templates/themes/" + themeName + "/setting.json";
        Resource resource = FileUtils.getResource(settingJsonPath);
        List<Option> optionList = JsonUtils.getObjectMapper()
                .readValue(resource.getInputStream(), new TypeReference<List<Option>>() {
                });

        String optionJson = AppConfig.getOption(BlogConst.OPTION_THEME_OPTION_PREFIX + themeName);
        if (optionJson != null) {
            Map<String, Option> nameToOption = JsonUtils.getObjectMapper().
                    readValue(optionJson, new TypeReference<List<Option>>() {
                    })
                    .stream().collect(Collectors.toMap(Option::getName, Function.identity()));

            optionList.forEach(option -> {
                if (nameToOption.containsKey(option.getName())) {
                    option.setValue(nameToOption.get(option.getName()).getValue());
                }
            });
        }

        return RestResponse.ok(optionList, 200);
    }


    /**
     * 获取当前激活的主题名称
     * Api 命名规则参考来源:
     * https://developer.github.com/v3/activity/starring/#list-repositories-starred-by-the-authenticated-user
     */
    @GetMapping("/theme/active")
    public RestResponse<String> getActiveTheme() {
        RestResponse<String> response = new RestResponse<>(true, HttpStatus.OK.value());
        response.setData(Common.blogTheme());
        return response;
    }

    /**
     * 激活指定主题
     */
    @PutMapping("/themes/{themeName}/active")
    public RestResponse<?> activateTheme(@PathVariable("themeName") String themeName) {
        siteService.activeTheme(themeName);
        return RestResponse.ok();
    }

    /**
     * 设置主题参数
     * TODO: @Validated加在集合参数上无效的问题
     */
    @PostMapping(path = "/themes/{themeName}/setting", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<?> saveThemeSetting(@PathVariable("themeName") String themeName,
                                            @Validated @RequestBody List<Option> optionList,
                                            BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail("参数不合法, 保存失败！");
        }
        optionList.forEach(option -> option.setCreateTime(null).setUpdateTime(null));
        Option option = new Option();
        option.setName(themeName);
        option.setValue(JsonUtils.toJsonString(optionList));
        option.setDescription("主题名为: '" + themeName + "' 的自定义参数设置");
        siteService.saveThemeSetting(option);
        return RestResponse.ok();
    }

    /**
     * 站点参数设置
     */
    @PostMapping(path = "/site/setting", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<?> saveSiteSetting(@RequestBody Map<String, String> parameterMap) {
        siteService.saveSiteSetting(parameterMap);
        return RestResponse.ok();
    }
}
