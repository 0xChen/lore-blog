package com.developerchen.core.system.web.view.admin;


import com.developerchen.core.base.BaseController;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 * 用户管理 前端控制器
 * </p>
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class ProfileAdminViewController extends BaseController {
    private final IUserService userService;

    public ProfileAdminViewController(IUserService userService) {
        this.userService = userService;
    }


    /**
     * 显示当前登陆用户的信息
     */
    @GetMapping("/profile")
    public String getProfile(Model model) {
        User user = userService.getUserById(getUserId());
        user.setPassword(null);
        model.addAttribute("user", user);
        return "admin/profile";
    }

}
