package com.developerchen.core.web;


import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.security.RefreshToken;
import com.developerchen.core.service.IUserService;
import com.developerchen.core.util.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 用户管理 前端控制器
 * </p>
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class UserAdminController extends BaseController {
    private final IUserService userService;

    public UserAdminController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 管理权限的用户新增或更新其他用户信息
     *
     * @param user 新用户信息
     * @return 用户信息
     */
    @ResponseBody
    @PostMapping("/users")
    public RestResponse<User> saveOrUpdate(User user) {
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
    @GetMapping("/users/{userId}")
    public RestResponse<User> getById(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return RestResponse.ok(user);
        } else {
            return RestResponse.fail("没有此用户");
        }
    }

    /**
     * 通过用户名称获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @ResponseBody
    @GetMapping("/users")
    public RestResponse<User> getByUsername(@RequestParam("username") String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            user.setPassword(null);
            return RestResponse.ok(user);
        } else {
            return RestResponse.fail("没有此用户");
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     */
    @ResponseBody
    @DeleteMapping("/users/{userId}")
    public RestResponse<String> deleteById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return RestResponse.ok();
    }

    /**
     * 当前登陆用户修改自己的密码
     * 此方法会更新前端的access_token
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    @RefreshToken
    @ResponseBody
    @PutMapping(path = "/user/password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RestResponse<String> updatePassword(@RequestParam String oldPassword,
                                               @RequestParam String newPassword) {
        User user = userService.getUserById(getUserId());
        if (!SecurityUtils.matchesUserPassword(oldPassword, user.getPassword())) {
            return RestResponse.fail(600, "原密码错误");
        }
        user.setPassword(newPassword);
        userService.saveOrUpdateUser(user);
        return RestResponse.ok("密码修改成功");
    }

    @RefreshToken
    @ResponseBody
    @PutMapping(path = "/user/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<String> updatePassword(@RequestBody Map<String, String> parameterMap) {
        String oldPassword = parameterMap.get("oldPassword");
        String newPassword = parameterMap.get("newPassword");

        return this.updatePassword(oldPassword, newPassword);
    }

    /**
     * 当前登陆用户修改自己的个人信息
     * 此方法会更新前端的access_token
     */
    @RefreshToken
    @ResponseBody
    @PutMapping("/user/profile")
    public RestResponse<String> updateProfile(@Validated @ModelAttribute User user, BindingResult result) {
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
     * 当前登陆用户修改自己的个人信息
     * 此方法会更新前端的access_token
     */
    @RefreshToken
    @ResponseBody
    @PutMapping(path = "/user/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<String> updateProfile(@RequestBody User user) {
        user.setId(getUserId());
        // 防御前端构造表单修改用户名
        user.setUsername(null);
        userService.saveOrUpdateUser(user);
        return RestResponse.ok();
    }

    /**
     * 显示当前登陆用户的信息
     */
    @GetMapping("/user/profile")
    public String getProfile(Model model) {
        User user = userService.getUserById(getUserId());
        user.setPassword(null);
        model.addAttribute("user", user);
        return "admin/profile";
    }

    /**
     * 获取当前登陆用户的信息
     */
    @ResponseBody
    @GetMapping(value = "/user/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<User> getProfile() {
        User user = userService.getUserById(getUserId());
        user.setPassword(null);
        return RestResponse.ok(user);
    }
}
