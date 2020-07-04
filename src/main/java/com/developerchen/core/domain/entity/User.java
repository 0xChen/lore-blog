package com.developerchen.core.domain.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author syc
 */
@Validated
@TableName("sys_user")
public class User extends BaseEntity {

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
    private Date lastLogin;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public String getNickname() {
        return nickname;
    }

    public User setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public User setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getRole() {
        return role;
    }

    public User setRole(String role) {
        this.role = role;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public User setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public User setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public User setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public User setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
