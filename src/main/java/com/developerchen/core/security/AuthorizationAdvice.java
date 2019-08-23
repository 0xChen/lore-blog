package com.developerchen.core.security;

import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IUserService;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 判定是否需要刷新客户端Cookie中的access_token.
 * 以下情况将重新授权客户端access_token:
 * 1.当前登陆用户修改了自己的数据时, user数据发生修改时会导致token过期, 所以重新授权
 *
 * @author syc
 */
@ControllerAdvice
public class AuthorizationAdvice implements ResponseBodyAdvice<Object> {

    private final IUserService userService;

    public AuthorizationAdvice(IUserService userService) {
        this.userService = userService;
    }

    /**
     * Whether this component supports the given controller method return type
     * and the selected {@code HttpMessageConverter} type.
     *
     * @param returnType    the return type
     * @param converterType the selected converter type
     * @return {@code true} if {@link #beforeBodyWrite} should be invoked;
     * {@code false} otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(RefreshToken.class);
    }

    /**
     * Invoked after an {@code HttpMessageConverter} is selected and just before
     * its write method is invoked.
     *
     * @param body                  the body to be written
     * @param returnType            the return type of the controller method
     * @param selectedContentType   the content type selected through content negotiation
     * @param selectedConverterType the converter type selected to write to the response
     * @param request               the current request
     * @param response              the current response
     * @return the body that was passed in or a modified (possibly new) instance
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        HttpServletRequest httpRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse httpResponse = ((ServletServerHttpResponse) response).getServletResponse();
        Long userId = (Long) httpRequest.getAttribute(Const.REQUEST_USER_ID);
        User user = userService.getUserById(userId);
        String token = JwtTokenUtil.generateToken(user);

        String contextPath = httpRequest.getContextPath();
        contextPath = contextPath.length() > 0 ? contextPath : "/";

        Cookie cookie = new Cookie(Const.COOKIE_ACCESS_TOKEN, token);
        cookie.setMaxAge((int) (JwtTokenUtil.EXPIRE_TIME / 1000));
        cookie.setPath(contextPath);
        cookie.setSecure(httpRequest.isSecure());
        cookie.setHttpOnly(true);
        httpResponse.addCookie(cookie);

        return body;
    }
}
