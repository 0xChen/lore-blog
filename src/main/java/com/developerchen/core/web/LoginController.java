package com.developerchen.core.web;

import com.developerchen.core.annotation.LogInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 登陆处理
 *
 * @author syc
 * @date 2018-11-10 21:26:53
 */
@Controller
@RequestMapping("/admin")
public class LoginController extends BaseController {

    /**
     * 后台管理登录页
     */
    @LogInfo(desc = "访问后台管理登录页")
    @RequestMapping("/login")
    public String login() {
        return "admin/login";
    }

}
