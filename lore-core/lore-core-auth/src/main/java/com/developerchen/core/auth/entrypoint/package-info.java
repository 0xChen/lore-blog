/**
 * 认证入口点实现
 * 
 * 包含处理未认证请求的入口点组件：
 * - DelegatingAuthenticationEntryPoint: 委托入口点，根据请求类型路由
 * - ApiAuthenticationEntryPoint: API 请求入口点，返回 JSON 错误响应
 * - FormAuthenticationEntryPoint: 表单请求入口点，重定向到登录页面
 */
package com.developerchen.core.auth.entrypoint;