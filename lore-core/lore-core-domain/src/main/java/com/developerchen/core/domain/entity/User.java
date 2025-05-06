package com.developerchen.core.domain.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author syc
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Validated
@TableName("sys_user")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Length(max = 25, message = "昵称长度不能超过 25 个字符")
    private String nickname;
    /**
     * 登陆用户名
     */
    @NotNull(message = "登陆用户名不能为空")
    @Length(min = 3, max = 25, message = "登陆用户名长度必须在 3-25 个字符之间")
    private String username;
    /**
     * 登陆密码
     */
    @NotNull(message = "登陆用户名不能为空")
    @Length(max = 255, message = "密码长度不能超过 255 个字符")
    private String password;
    /**
     * 电子邮箱
     */
    @Email(message = "电子邮箱格式不正确")
    private String email;
    /**
     * 状态
     */
    private String status;
    /**
     * 角色
     */
    private String role;
    /**
     * 描述
     */
    @Length(max = 255, message = "描述长度不能超过 255 个字符")
    private String description;
    /**
     * 最近一次登陆时间
     */
    private LocalDateTime lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
