package com.developerchen.core.system.web.view.admin;

import cn.hutool.core.io.IoUtil;
import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.util.FileUtils;
import com.developerchen.core.logging.annotation.OperationLog;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * 登陆处理
 *
 * @author syc
 * @date 2018-11-10 21:26:53
 */
@Controller
@RequestMapping("/admin")
public class LoginAdminViewController extends BaseController {

    /**
     * 后台管理首页
     */
    @OperationLog(desc = "访问后台管理首页")
    @GetMapping("/index")
    public void login(ServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            Resource resource = FileUtils.getResource("classpath:/admin/index.html");
            outputStream.write(IoUtil.readBytes(resource.getInputStream(), true));
            outputStream.flush();
        }
    }

}
