package com.developerchen.core.system.web.admin;


import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.security.RefreshToken;
import com.developerchen.core.system.service.IUserService;
import com.developerchen.core.system.util.SecurityUtils;
import org.springframework.http.MediaType;
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
@RestController
@RequestMapping("/admin/profile")
public class ProfileAdminController extends BaseController {
    private final IUserService userService;

    public ProfileAdminController(IUserService userService) {
        this.userService = userService;
    }


    /**
     * 当前登陆用户修改自己的密码
     * 此方法会更新前端的access_token
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    @RefreshToken
    @PostMapping(path = "/password/update", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public R<String> updatePassword(@RequestParam String oldPassword,
                                    @RequestParam String newPassword) {
        User user = userService.getUserById(getUserId());
        if (!SecurityUtils.matchesUserPassword(oldPassword, user.getPassword())) {
            return R.fail(600, "原密码错误");
        }
        user.setPassword(newPassword);
        userService.upsert(user);
        return R.ok("密码修改成功");
    }

    @RefreshToken
    @PostMapping(path = "/password/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<String> updatePassword(@RequestBody Map<String, String> parameterMap) {
        String oldPassword = parameterMap.get("oldPassword");
        String newPassword = parameterMap.get("newPassword");

        return this.updatePassword(oldPassword, newPassword);
    }

    /**
     * 当前登陆用户修改自己的个人信息
     * 此方法会更新前端的access_token
     */
    @RefreshToken
    @PostMapping("/update")
    public R<String> updateProfile(@Validated @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return R.fail();
        }
        user.setId(getUserId());
        // 防御前端构造表单修改用户名
        user.setUsername(null);
        userService.upsert(user);
        return R.ok();
    }

    /**
     * 当前登陆用户修改自己的个人信息
     * 此方法会更新前端的access_token
     */
    @RefreshToken
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<String> updateProfile(@RequestBody User user) {
        user.setId(getUserId());
        // 防御前端构造表单修改用户名
        user.setUsername(null);
        userService.upsert(user);
        return R.ok();
    }

    /**
     * 获取当前登陆用户的信息
     */
    @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<User> getProfile() {
        User user = userService.getUserById(getUserId());
        user.setPassword(null);
        return R.ok(user);
    }
}
