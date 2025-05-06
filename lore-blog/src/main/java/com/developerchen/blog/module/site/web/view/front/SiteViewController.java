package com.developerchen.blog.module.site.web.view.front;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.constant.Const;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 站点
 *
 * @author syc
 */
@Controller
public class SiteViewController extends BaseController {

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


    /**
     * 安装页面
     */
    @GetMapping(BlogConst.INSTALL_URI)
    public String install() {
        return "install";
    }
}
