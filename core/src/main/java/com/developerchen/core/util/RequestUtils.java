package com.developerchen.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Request工具类
 *
 * @author syc
 */
public class RequestUtils {

    private static final List<MediaType> TARGET_MEDIA_TYPES = Arrays.asList(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json")
    );


    /**
     * 判断是否 ajax 调用
     * 1. 处理请求的方法是否有@ResponseBody注解
     * 2. 方法所在类是否有@RestController注解
     * 3. HttpServletRequest头信息中X-Requested-With是否等于XMLHttpRequest
     * 4. request header accept 是否含有json
     *
     * @param method 抛出异常的方法
     * @return true or not
     */
    public static boolean isAjaxRequest(HttpServletRequest request, Object method) {
        if (method instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(ResponseBody.class)) {
                return true;
            }
            Class<?> methodClass = handlerMethod.getMethod().getDeclaringClass();
            return methodClass.isAnnotationPresent(RestController.class);
        }
        return RequestUtils.isAjaxRequest(request);
    }


    /**
     * 判断request是否是ajax请求
     *
     * @param request the current request
     * @return boolean
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        request = getRequest(request);

        // 检查 X-Requested-With 头
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }

        // 获取 Accept 头并解析为 MediaType 列表
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return false;
        }

        List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);

        return mediaTypes.stream().anyMatch(mediaType ->
                TARGET_MEDIA_TYPES.stream().anyMatch(targetMediaType -> targetMediaType.includes(mediaType))
        );
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
