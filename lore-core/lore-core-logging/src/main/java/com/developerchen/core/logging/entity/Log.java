package com.developerchen.core.logging.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * <p>
 * 日志表
 * </p>
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_log")
public class Log extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志类型
     */
    private String type;
    /**
     * 日志描述
     */
    private String description;
    /**
     * 创建人
     */
    private Long createUserId;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 操作人IP地址
     */
    private String ip;
    /**
     * 用户标识
     */
    private String userAgent;
    /**
     * 请求URI
     */
    private String requestUri;
    /**
     * 请求URL后的查询参数
     */
    private String requestQuery;
    /**
     * 请求方法(get, post, ...)
     */
    private String requestMethod;
    /**
     * 调用方法
     */
    private String method;
    /**
     * 调用方法耗时ms
     */
    private Integer elapsedTime;
    /**
     * 调用方法的入参
     */
    private String arguments;
    /**
     * 异常信息
     */
    private String exception;
    /**
     * 异常堆栈信息
     */
    private String stackTrace;


}


