package com.developerchen.blog.module.site.web;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    @GetMapping(value = {"", "/index"})
    public String index(@RequestParam(defaultValue = "1") Long page,
                        @RequestParam(required = false) Long size,
                        Model model) {
        size = size == null ? Const.PAGE_DEFAULT_SIZE : size;
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "themes/{theme}/index";
    }

    /**
     * 安装页面
     */
    @GetMapping(BlogConst.INSTALL_URI)
    public String install() {
        return "install";
    }

    /**
     * 保存安装页面的用户自定义配置
     */
    @ResponseBody
    @PostMapping(BlogConst.INSTALL_URI + "/option")
    public RestResponse installOption(@RequestParam Map<String, String> parameterMap) {
        this.siteService.installOption(parameterMap);
        return RestResponse.ok();
    }

    /**
     * 添加一个管理账户
     */
    @ResponseBody
    @PostMapping(BlogConst.INSTALL_URI + "/user")
    public RestResponse installAdminUser(@Validated User user) {
        try {
            this.siteService.installAdminUser(user);
        } catch (Exception e) {
            return RestResponse.fail("初始化站点失败, 无法新增系统管理员用户.");
        }
        return RestResponse.ok(user);
    }

    /**
     * 锁定安装程序
     */
    @ResponseBody
    @PostMapping(BlogConst.INSTALL_URI + "/lock")
    public RestResponse installLock() {
        String errorMessage = null;
        try {
            File parent = BlogConst.INSTALLED.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException(parent.getPath() + "目录创建失败. ");
                }
            }
            String content = "Lore-Blog安装后自动生成的文件, 用于锁定重复安装. 删除此文件才可以重新执行安装程序. ";
            Files.write(BlogConst.INSTALLED.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (errorMessage != null) {
            BlogConst.HAS_INSTALLED = false;
            return RestResponse.fail("初始化站点失败, 无法创建["
                    + BlogConst.INSTALLED.getPath() + "]文件, " + errorMessage + ". ");
        } else {
            BlogConst.HAS_INSTALLED = true;
            return RestResponse.ok();
        }
    }
}
