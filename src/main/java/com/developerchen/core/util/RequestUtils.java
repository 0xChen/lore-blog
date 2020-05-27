package com.developerchen.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Request工具类
 *
 * @author syc
 */
public class RequestUtils {


    public static Map<String, String> readValue(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> jsonMap = objectMapper.readValue(request.getInputStream(),
                    new TypeReference<Map<String, String>>(){});
            System.out.println(jsonMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断request是否是ajax请求
     *
     * @param request the current request
     * @return boolean
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        request = getRequest(request);
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        return request.getContentType().toLowerCase().contains("json");
    }

    /**
     * 获取request中的URL
     *
     * @return URL String
     */
    public static String getRequestURI() {
        return getRequestURI(null);
    }

    /**
     * 获取request中的URL
     *
     * @param request the current request
     * @return URL
     */
    public static String getRequestURI(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取request中URL的查询参数
     *
     * @return URL中的查询参数
     */
    public static String getRequestQueryString() {
        return getRequestQueryString(null);
    }

    /**
     * 获取request中URL的查询参数
     *
     * @param request the current request
     * @return URL中的查询参数
     */
    public static String getRequestQueryString(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getQueryString() : null;
    }

    /**
     * 获取request请求方法(get, post, ...)
     *
     * @return 请求方法
     */
    public static String getRequestMethod() {
        return getRequestMethod(null);
    }

    /**
     * 获取request请求方法(get, post, ...)
     *
     * @param request the current request
     * @return 请求方法
     */
    public static String getRequestMethod(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getMethod() : null;
    }

    public static HttpServletRequest getRequest(HttpServletRequest request) {
        if (request == null) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                request = requestAttributes.getRequest();
            }
        }
        return request;
    }
}
