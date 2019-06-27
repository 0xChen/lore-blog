package com.developerchen.core.exception;

import com.developerchen.core.domain.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author syc
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理异常
     * 只处理Ajax请求产生的异常, 其他请求的异常会继续抛到下一层处理
     *
     * @param ex      the exception
     * @param method  the method
     * @param request the current request
     */
    @ResponseBody
    @ExceptionHandler
    public RestResponse<?> handleException(HttpServletRequest request,
                                           HandlerMethod method,
                                           Exception ex) throws Exception {

        if (isAjaxRequest(request, method)) {
            HttpStatus status = getStatus(request);
            int code = status.value();
            String message = null;
            if (ex instanceof TipException) {
                message = ex.getMessage();
            } else if (ex instanceof RestException) {
                code = ((RestException) ex).getCode();
                message = ex.getMessage();
            } else if (ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else if (ex instanceof BindException) {
                message = resolveBindException(ex);
            }
            ex.printStackTrace();
            return RestResponse.fail(code, message);
        } else {
            /*
             * Rethrow the given exception for further processing through the HandlerExceptionResolver chain.
             * The default HandlerExceptionResolver {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}
             */
            throw ex;
        }
    }

    /**
     * 解决binding error
     *
     * @param ex 异常
     * @return 异常的文本描述
     */
    private String resolveBindException(Exception ex) {
        List<String> errorMessageList = ((BindException) ex).getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());
        return StringUtils.join(errorMessageList, ";");
    }

    /**
     * 获取错误状态码
     *
     * @param request the current request
     */
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

    /**
     * 判断是否 ajax 调用
     * 1. HttpServletRequest头信息中X-Requested-With是否等于XMLHttpRequest
     * 2. 处理请求的方法是否有@ResponseBody注解
     * 3. 方法所在类是否有@RestController注解
     *
     * @param method 抛出异常的方法
     * @return true or not
     */
    @SuppressWarnings("unchecked")
    private boolean isAjaxRequest(HttpServletRequest request, Object method) {
        String headerValue = request.getHeader("X-Requested-With");
        String ajaxHeader = "XMLHttpRequest";
        if (ajaxHeader.equals(headerValue)) {
            return true;
        }
        if (method instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) method;
            if (handlerMethod.hasMethodAnnotation(ResponseBody.class)) {
                return true;
            }
            Class methodClass = handlerMethod.getMethod().getDeclaringClass();
            return methodClass.isAnnotationPresent(RestController.class);
        }
        return false;
    }

}
