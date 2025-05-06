package com.developerchen.core.system.web.admin;


import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户管理 前端控制器
 * </p>
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
public class UserAdminController extends BaseController {
    private final IUserService userService;


    /**
     * 管理权限的用户新增或更新其他用户信息
     *
     * @param user 新用户信息
     * @return 用户信息
     */
    @PostMapping("/upsert")
    public R<User> upsert(User user) {
        userService.upsert(user);
        return R.ok(user);
    }

    /**
     * 通过用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}/detail")
    public R<User> getUserById(@PathVariable("userId") Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return R.ok(user);
        } else {
            return R.fail("没有此用户");
        }
    }

    /**
     * 通过用户名称获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}/detail")
    public R<User> getUserByUsername(@PathVariable("username") String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            user.setPassword(null);
            return R.ok(user);
        } else {
            return R.fail("没有此用户");
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     */
    @PostMapping("/{userId}/delete")
    public R<String> deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return R.ok();
    }
}
