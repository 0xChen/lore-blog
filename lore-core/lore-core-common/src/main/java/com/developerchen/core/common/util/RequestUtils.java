package com.developerchen.core.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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
     * 判断是否 api 调用
     * 1. 处理请求的方法是否有@ResponseBody注解
     * 2. 方法所在类是否有@RestController注解
     * 3. request header accept 是否含有json
     *
     * @param method 抛出异常的方法
     * @return true or not
     */
    public static boolean isApiRequest(HttpServletRequest request, Object method) {
        if (method instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(ResponseBody.class)) {
                return true;
            }
            Class<?> methodClass = handlerMethod.getMethod().getDeclaringClass();
            return methodClass.isAnnotationPresent(RestController.class);
        }
        return RequestUtils.isApiRequest(request);
    }


    /**
     * 判断request是否是 api 请求
     *
     * @param request the current request
     * @return boolean
     */
    public static boolean isApiRequest(HttpServletRequest request) {
        request = getRequest(request);

        // 获取 Content-Type 头并解析为 MediaType 列表
        String contentType = request.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            return false;
        }

        List<MediaType> mediaTypes = MediaType.parseMediaTypes(contentType);

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

    public static String getRemoteIp() {
        return getRemoteIp(null);
    }

    /**
     * 通过request获取调用者的IP
     * 会以请求头中的key "X-Forwarded-For"判断是否有反向代理以获取真实IP
     *
     * @param request the current request
     * @return real ip
     */
    public static String getRemoteIp(HttpServletRequest request) {
        String separatorChar = ",";
        request = getRequest(request);
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            if (StringUtils.contains(ip, separatorChar)) {
                ip = ip.split(separatorChar)[0];
            }
        } else {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUserAgent() {
        return getUserAgent(null);
    }

    /**
     * 通过request获取User-Agent
     *
     * @param request the current request
     * @return agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getHeader("User-Agent") : null;
    }
}
