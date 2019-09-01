package com.developerchen.core.web;

import com.developerchen.core.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * 基础控制器
 * 所有其他前端控制器的父类, 提供一些公用方法及成员变量
 *
 * @author syc
 */
public abstract class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected HttpServletRequest request;

    /**
     * 从HttpServletRequest中获取当前登陆用户ID
     *
     * @return 登陆用户ID
     */
    protected Long getUserId() {
        return (Long) request.getAttribute(Const.REQUEST_USER_ID);
    }

    /**
     * 从HttpServletRequest中获取当前登陆用户名
     *
     * @return 登陆用户名
     */
    protected String getUsername() {
        return (String) request.getAttribute(Const.REQUEST_USER_NAME);
    }

    /**
     * 返回404页面
     */
    protected String page404() {
        return "error/404";
    }
}
