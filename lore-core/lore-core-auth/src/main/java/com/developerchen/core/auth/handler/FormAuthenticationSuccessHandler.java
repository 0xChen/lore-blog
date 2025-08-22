package com.developerchen.core.auth.handler;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 表单认证成功处理器
 * 处理表单登录成功后的重定向逻辑
 * 
 * @author syc
 */
@Component
public class FormAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public FormAuthenticationSuccessHandler() {
        // 设置默认的成功跳转URL
        setDefaultTargetUrl("/");
        // 总是使用默认目标URL（可选配置）
        setAlwaysUseDefaultTargetUrl(false);
        // 设置目标URL参数名
        setTargetUrlParameter("redirectTo");
    }
}