package com.developerchen.core.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回对象
 *
 * 自定义Http状态码从600开始
 * 600表示有需要alert的错误消息
 * 611表示有表示博客没有执行过安装初始化步骤, 需要执行安装程序
 * @param <T>
 * @author syc
 */
public class RestResponse<T> {

    /**
     * 响应数据
     */
    private T data;

    /**
     * 扩展数据
     */
    private Map<String, Object> extData = new HashMap<>(16);

    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 信息
     */
    private String message;

    /**
     * Http状态码
     */
    private int status = -1;

    /**
     * 服务器响应时间
     */
    private long timestamp;

    public RestResponse() {
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public RestResponse(boolean success) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
    }

    public RestResponse(boolean success, int status) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
        this.status = status;
    }

    public RestResponse(boolean success, T data) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
        this.data = data;
    }

    public RestResponse(boolean success, T data, int status) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
        this.data = data;
        this.status = status;
    }

    public RestResponse(boolean success, String message) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
        this.message = message;
    }

    public RestResponse(boolean success, String message, int status) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.success = success;
        this.message = message;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getExtData() {
        return extData;
    }

    public void setExtData(Map<String, Object> extData) {
        this.extData = extData;
    }

    public void addExtData(String key, Object value) {
        this.extData.put(key, value);
    }

    public void addExtData(Long key, Object value) {
        this.extData.put(String.valueOf(key), value);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public static <T> RestResponse<T> ok() {
        return new RestResponse<>(true, 200);
    }

    public static <T> RestResponse<T> ok(T payload) {
        return new RestResponse<>(true, payload, 200);
    }

    public static <T> RestResponse<T> ok(String message) {
        return new RestResponse<>(true, message, 200);
    }

    public static <T> RestResponse<T> ok(int status) {
        return new RestResponse<>(true, null, status);
    }

    public static <T> RestResponse<T> ok(T payload, int status) {
        return new RestResponse<>(true, payload, status);
    }


    public static <T>RestResponse<T> fail() {
        return new RestResponse<>(false);
    }

    public static <T> RestResponse<T> fail(String message) {
        return new RestResponse<>(false, message);
    }

    public static <T> RestResponse<T> fail(int status) {
        return new RestResponse<>(false, null, status);
    }

    public static <T> RestResponse<T> fail(int status, String message) {
        return new RestResponse<>(false, message, status);
    }

}