package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * <p>
 * 日志表
 * </p>
 *
 * @author syc
 */
@TableName("sys_log")
public class Log extends BaseEntity {

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


    public String getType() {
        return type;
    }

    public Log setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getCreateUserId() {
        return createUserId;
    }

    public Log setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Log setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public Log setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Log setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public Log setRequestUri(String requestUri) {
        this.requestUri = requestUri;
        return this;
    }

    public String getRequestQuery() {
        return requestQuery;
    }

    public Log setRequestQuery(String requestQuery) {
        this.requestQuery = requestQuery;
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public Log setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Log setMethod(String method) {
        this.method = method;
        return this;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public Log setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    public String getArguments() {
        return arguments;
    }

    public Log setArguments(String params) {
        this.arguments = arguments;
        return this;
    }

    public String getException() {
        return exception;
    }

    public Log setException(String exception) {
        this.exception = exception;
        return this;
    }

}
