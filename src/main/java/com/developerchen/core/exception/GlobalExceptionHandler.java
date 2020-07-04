package com.developerchen.core.exception;

import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.util.RequestUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author syc
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler
    public RestResponse<?> handleException(HttpServletRequest request,
                                           ServletException ex) throws Exception {
        if (RequestUtils.isAjaxRequest(request)) {
            HttpStatus status = getStatus(request);
            ex.printStackTrace();
            return RestResponse.fail(status.value(), ex.getMessage());
        } else {
            /*
             * Rethrow the given exception for further processing through the HandlerExceptionResolver chain.
             * The default HandlerExceptionResolver {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}
             */
            throw ex;
        }
    }

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
                                           Exception ex,
                                           @Nullable HandlerMethod method) throws Exception {
        boolean needPrintStackTrace = true;
        if (RequestUtils.isAjaxRequest(request, method)) {
            HttpStatus status = getStatus(request);
            int code = status.value();
            String message = null;
            if (ex instanceof AlertException) {
                message = ex.getMessage();
                needPrintStackTrace = false;
            } else if (ex instanceof RestException) {
                code = ((RestException) ex).getCode();
                message = ex.getMessage();
                needPrintStackTrace = false;
            } else if (ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else if (ex instanceof BindException) {
                message = resolveBindException(ex);
            } else if (ex instanceof HttpMessageNotReadableException) {
                message = ex.getMessage();

                //以下开始数据库相关异常
            } else if (ex instanceof DuplicateKeyException) {
                message = ex.getCause().getMessage();
            }

            if (needPrintStackTrace) {
                ex.printStackTrace();
            }
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
        String errorMessage = ((BindException) ex).getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return errorMessage;
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
}
