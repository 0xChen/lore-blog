package com.developerchen.core.constant;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统常量
 *
 * @author syc
 */
@SuppressWarnings("all")
public final class Const {

    private Const() {
    }

    /**
     * 所有的静态常量都会添加到此map中包括其他module, 为了Thymeleaf在页面将所有常量生成对应的js常量
     */
    public static final Map<String, Object> CONST_MAP = new HashMap<>(30);

    public static void init() throws IllegalAccessException {
        for (Field field : Const.class.getFields()) {
            if (!field.getType().isAssignableFrom(Map.class)) {
                CONST_MAP.put(field.getName(), field.get(null));
            }
        }
    }

    /**
     * 域名
     */
    public static String HOSTNAME;

    /**
     * 是/否
     */
    public static final String YES = "1";
    public static final String NO = "0";

    /**
     * 分页相关
     */
    public static final String PAGE_NEXT = "next";// 下一页
    public static final String PAGE_PREV = "prev";// 上一页
    public static final long PAGE_DEFAULT_SIZE = 10;// 默认每页数量

    /**
     * 角色
     */
    public static final String ROLE_ADMIN = "1";// 管理员
    public static final String ROLE_USER = "2";// 普通用户
    public static final String ROLE_GUEST = "3";// 访客

    /**
     * 用户状态
     */
    public static final String USER_STATUS_DISABLED = "0";// 禁用
    public static final String USER_STATUS_ENABLED = "1";// 正常
    public static final String USER_STATUS_LOCKED = "2";// 锁定
    public static final String USER_STATUS_EXPIRED = "3";// 过期

    /**
     * log类型
     */
    public static final String LOG_INFO = "info";// 普通信息日志
    public static final String LOG_EMAIL = "email";// Email日志
    public static final String LOG_EXCEPTION = "exception";// 异常日志

    /**
     * Request key
     */
    public static final String REQUEST_USER_ID = "userId";// 放置当前登陆用户的ID
    public static final String REQUEST_USER_NAME = "username";// 放置当前登陆用户的用户名

    /**
     * Cookie key
     */
    public static final String COOKIE_ACCESS_TOKEN = "access_token";// 权限认证凭据

}
