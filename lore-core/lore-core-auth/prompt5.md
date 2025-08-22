# 角色
你是一位经验丰富的 Spring Security 专家，熟悉 Spring Security 6.4+ 和 Spring Security OAuth2 的最新特性和最佳实践。你熟悉各种身份验证机制、JWT、无状态/有状态会话管理以及安全最佳实践。

# 任务
根据以下需求，设计并提供一个 Spring Boot 应用的 Spring Security 6.4.5+（最新稳定版）的核心配置代码和相关组件实现。重点在于采用组件化、可配置的设计，并遵循 Spring Security 的最佳实践，减少自定义过滤器。

# 需求详情

## 1. 技术栈与版本：
* Spring Security: 6.4.5+
* Spring Boot: 3.2.x+ (与所选 Spring Security 版本兼容的最新稳定版)
* Java: 17+

## 2. 认证功能：

### 2.1 多种登录方式：
* **表单登录 (Form Login)：**
    * 通过标准 HTML 表单提交的用户名密码认证
    * 路径：`/login` (POST，`application/x-www-form-urlencoded`)
* **JSON 登录 (JSON Login)：**
    * 通过 JSON 格式提交的用户名密码认证
    * 路径：`/api/login` (POST，`application/json`)
    * 请求体格式：`{"username": "user", "password": "pass"}`

### 2.2 认证响应：
* **表单登录成功：**
    * 重定向到配置的成功页面（如 `/` 或 `/dashboard`）
* **JSON 登录成功：**
    * 返回 HTTP 200 状态码
    * 返回 JSON 格式的 Token 信息：
  ```json
  {
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "tokenType": "Bearer",
    "expiresIn": 1800
  }
  ```
* **登录失败：**
    * 表单登录：重定向到失败页面，显示错误信息
    * JSON 登录：返回 HTTP 401 状态码和错误信息
  ```json
  {
    "timestamp": "2023-06-06T12:34:56.789Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "无效的用户名或密码"
  }
  ```

### 2.3 Token 管理：
* **生成方式：**
    * Access Token：JWT 格式
    * Refresh Token：不透明字符串（UUID）
* **过期时间：**
    * Access Token：默认 30 分钟（可配置）
    * Refresh Token：默认 7 天（可配置）
* **刷新机制：**
    * 端点：`/api/token/refresh` (POST)
    * 请求体：`{"refreshToken": "your_refresh_token"}`
    * 成功响应：新的 Access Token 和 Refresh Token
    * 失败响应：HTTP 401/403 状态码和错误信息

### 2.4 会话管理模式：
* **可配置的双模式：**
    * 有状态模式 (Stateful)：使用 Session + Token
    * 无状态模式 (Stateless)：仅使用 Token
* **配置方式：**
    * 通过 `application.yml` 或 `properties` 文件切换
  ```yaml
  app:
    security:
      session:
        mode: stateless  # stateful | stateless
  ```

### 2.5 其他功能：
* **记住我 (Remember-Me)：** 支持基于 Cookie 的自动登录（有状态模式下）
* **JWT 配置：** 支持自定义密钥、过期时间、签发者和算法
* **错误处理：** 统一的错误响应格式和适当的 HTTP 状态码

## 3. 实现要求：

### 3.1 代码架构：
* **组件化设计：** 各功能模块应解耦，便于维护和扩展
* **配置分离：** 将配置参数外部化，支持不同环境的配置
* **灵活性：** 支持在不修改核心代码的情况下扩展或替换组件

### 3.2 关键组件设计：
* **SecurityConfig：** 主要安全配置，支持动态切换有状态/无状态模式
* **认证转换器：** 处理不同格式的认证请求
    * `JsonLoginAuthenticationConverter`：处理 JSON 格式的登录请求
    * `TokenAuthenticationConverter`：处理 Token 认证
* **认证过滤器：** 处理不同认证方式
    * `ApiLoginAuthenticationFilter`：处理 API 登录
    * `AuthenticationFilter`：处理 Token 认证
* **认证提供者：** 验证不同类型的认证凭证
    * `DaoAuthenticationProvider`：用户名密码认证
    * `JwtAuthenticationProvider`：JWT 认证
    * `OpaqueTokenAuthenticationProvider`：不透明 Token 认证
* **Token 服务：** 生成和验证 Token
    * `TokenService`：Token 生成和管理
    * `RefreshTokenService`：Refresh Token 管理
* **处理器：** 处理认证成功和失败
    * `ApiAuthenticationSuccessHandler`：API 认证成功处理
    * `ApiAuthenticationFailureHandler`：API 认证失败处理

### 3.3 最佳实践遵循：
* **优先使用内置组件：** 尽量使用 Spring Security 和 OAuth2 的内置组件
* **遵循认证流程：** 使用 `AuthenticationConverter` -> `AuthenticationManager` -> `AuthenticationProvider` 的流程
* **避免直接操作 SecurityContext：** 通过 Spring Security 的标准机制设置认证
* **正确使用响应处理器：** 根据不同的认证方式返回适当的响应

## 4. 核心代码需求：

请提供以下关键组件的完整实现代码：

1. **SecurityConfig.java**：主要安全配置
2. **JsonLoginAuthenticationConverter.java**：JSON 登录请求转换器
3. **TokenAuthenticationConverter.java**：Token 认证转换器
4. **ApiLoginAuthenticationFilter.java**：API 登录过滤器
5. **ApiAuthenticationSuccessHandler.java**：API 认证成功处理器
6. **ApiAuthenticationFailureHandler.java**：API 认证失败处理器
7. **TokenService.java**：Token 服务
8. **RefreshTokenService.java**：Refresh Token 服务接口
9. **InMemoryRefreshTokenService.java**：内存实现
10. **TokenController.java**：Token 刷新控制器
11. **SecurityProperties.java**：安全配置属性
12. **application.yml**：示例配置

此外，请提供关键组件的架构图和实现说明，解释各组件之间的关系和职责，以及认证流程。

## 5. 思考方向：

在实现过程中，请重点考虑以下方面：

1. **安全性**：如何防止常见的安全问题，如 CSRF、XSS、JWT 相关风险等
2. **可扩展性**：如何设计组件使其易于扩展，支持新的认证方式和需求
3. **可测试性**：如何确保代码易于测试
4. **性能**：如何优化认证过程的性能，特别是在高并发场景下
5. **用户体验**：如何处理各种错误情况并提供有用的反馈

请开始设计并提供完整的代码实现。
