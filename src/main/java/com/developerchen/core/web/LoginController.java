package com.developerchen.core.web;

import cn.hutool.core.io.IoUtil;
import com.developerchen.core.annotation.LogInfo;
import com.developerchen.core.util.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
     * 后台管理首页
     */
    @LogInfo(desc = "访问后台管理首页")
    @GetMapping("/index")
    public void login(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            Resource resource = FileUtils.getResource("classpath:/admin/index.html");
            outputStream.write(IoUtil.readBytes(resource.getInputStream(), true));
            outputStream.flush();
        }
    }

}
