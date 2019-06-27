package com.developerchen.blog.module.site.web;

import com.developerchen.core.constant.Const;
import com.developerchen.core.web.BaseController;
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
public class SiteController extends BaseController {

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

}
