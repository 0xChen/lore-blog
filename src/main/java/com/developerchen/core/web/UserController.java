package com.developerchen.core.web;


import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.security.RefreshToken;
import com.developerchen.core.service.IUserService;
import com.developerchen.core.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户管理 前端控制器
 * </p>
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class UserController extends BaseController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 管理权限的用户新增或更新其他用户信息
     *
     * @param user 新用户信息
     * @return 用户信息
     */
    @ResponseBody
    @PostMapping("/user")
    public RestResponse saveOrUpdate(User user) {
        userService.saveOrUpdateUser(user);
        return RestResponse.ok(user);
    }

    /**
     * 通过用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @ResponseBody
    @GetMapping("/user/{userId}")
    public RestResponse getById(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        return user != null ? RestResponse.ok(user) : RestResponse.fail("没有此用户");
    }

    /**
     * 通过用户名称获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @ResponseBody
    @GetMapping("/api/user")
    public RestResponse getByUsername(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        return user != null ? RestResponse.ok(user) : RestResponse.fail("没有此用户");
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     */
    @ResponseBody
    @DeleteMapping("/user/{userId}")
    public RestResponse deleteById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return RestResponse.ok();
    }

    /**
     * 管理权限的用户更新其他用户的密码
     *
     * @param userId      被更新的用户ID
     * @param newPassword 新密码
     */
    @ResponseBody
    @PutMapping("/user/password")
    public RestResponse updateUserPassword(@RequestParam Long userId,
                                           @RequestParam String newPassword) {
        User user = userService.getUserById(userId);
        user.setPassword(newPassword);
        userService.saveOrUpdateUser(user);
        return RestResponse.ok("密码修改成功");
    }

    /**
     * 当前登陆用户修改自己的密码
     * 此方法会更新前端的access_token
     *
     * @param rawPassword 原密码
     * @param newPassword 新密码
     */
    @RefreshToken
    @ResponseBody
    @PutMapping("/password")
    public RestResponse updatePassword(@RequestParam String rawPassword,
                                       @RequestParam String newPassword) {
        User user = userService.getUserById(getUserId());
        if (!SecurityUtils.matchesUserPassword(rawPassword, user.getPassword())) {
            return RestResponse.fail("原密码错误");
        }
        user.setPassword(newPassword);
        userService.saveOrUpdateUser(user);
        return RestResponse.ok("密码修改成功");
    }

    /**
     * 当前登陆用户修改自己的信息
     * 此方法会更新前端的access_token
     */
    @RefreshToken
    @ResponseBody
    @PutMapping("/profile")
    public RestResponse updateLoginUser(@ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return RestResponse.fail();
        }
        user.setId(getUserId());
        // 防御前端构造表单修改用户名
        user.setUsername(null);
        userService.saveOrUpdateUser(user);
        return RestResponse.ok();
    }

    /**
     * 显示当前登陆用户的信息
     */
    @GetMapping("/profile")
    public String loginUser(Model model) {
        User user = userService.getUserById(getUserId());
        model.addAttribute("user", user);
        return "/admin/profile";
    }
}
