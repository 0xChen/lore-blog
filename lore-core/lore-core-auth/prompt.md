# 角色
你是一位经验丰富的 Spring Security 专家，熟悉 Spring Security 6.4+ 和 Spring Security OAuth2 的最新特性和最佳实践。

# 任务
根据以下需求，设计并提供一个 Spring Boot 应用的 Spring Security 6.4.5 (或更高6.+版本) 的核心配置代码和相关组件实现。重点在于尽可能利用 Spring Security 和 Spring Security OAuth2 的内置组件和机制，而不是手动创建过多的自定义 Filter。

# 需求详情

## 1. 版本要求：
*   Spring Security: 6.4.5 (最低 6.0+)
*   Spring Boot: (请选择一个与所选 Spring Security 版本兼容的最新稳定版，例如 3.2.x 或 3.3.x)

## 2. 功能描述：

### 2.1 多种密码提交方式登录：
    *   **2.1.1 表单提交方式的用户名密码登录 (Form Login):**
        *   标准 HTML 表单提交 (`application/x-www-form-urlencoded`)。
        *   路径：例如 `/login` (POST)
    *   **2.1.2 JSON 提交方式的用户名密码登录 (JSON Login):**
        *   POST 请求，Content-Type 为 `application/json`。
        *   请求体包含 `username` 和 `password` 字段。
        *   路径：例如 `/api/login` (POST)

### 2.2 账号密码认证成功后：
    *   **2.2.1 返回 Token:**
        *   Access Token: 生成 JWT 格式的 Token。
        *   Refresh Token: 生成一个安全的、通常是不透明的字符串（例如 UUID）。
        *   包含这两个 token 在响应中。

### 2.3 Token 过期时间：
    *   **2.3.1 Access Token 过期时间:**
        *   可配置，默认 30 分钟。
    *   **2.3.2 Access Token 过期后:**
        *   后续请求携带过期 Access Token 时，返回 HTTP 401 错误码。
        *   响应体中应包含明确的错误信息，指示 token 过期。
    *   **2.3.3 使用 Refresh Token 刷新:**
        *   前端应能使用 Refresh Token 调用特定端点刷新 Access Token。

### 2.4 Token 刷新：
    *   **2.4.1 刷新端点:**
        *   例如 `/api/token/refresh` (POST)。
        *   请求体包含 `refresh_token`。
    *   **2.4.2 刷新成功:**
        *   返回新的 Access Token 和新的 Refresh Token (可选，推荐也刷新 Refresh Token 以增强安全性)。
    *   **2.4.3 Refresh Token 失效/无效:**
        *   返回 HTTP 401 或 403 错误码，要求用户重新登录。

### 2.5 登录成功/失败响应处理：
    *   **2.5.1 表单登录:**
        *   成功：重定向到配置的成功 URL (例如，类似 `AbstractAuthenticationTargetUrlRequestHandler` 的行为)。
        *   失败：重定向到配置的失败 URL，并携带错误信息 (例如，类似 `SimpleUrlAuthenticationFailureHandler` 的行为)。
    *   **2.5.2 JSON 登录:**
        *   成功：返回 JSON 响应，包含 Access Token, Refresh Token, token 类型 (e.g., "Bearer"), 过期时间等。
            ```json
            {
              "access_token": "your_jwt_access_token",
              "refresh_token": "your_opaque_refresh_token",
              "token_type": "Bearer",
              "expires_in": 1800 // seconds
            }
            ```
        *   失败：返回 JSON 响应，包含 HTTP 状态码 (例如 401) 和错误信息。
            ```json
            {
              "timestamp": "yyyy-MM-ddTHH:mm:ss.SSSZ",
              "status": 401,
              "error": "Unauthorized",
              "message": "Invalid credentials"
            }
            ```

### 2.6 基于 Cookie 的记住我功能 (Remember-Me):
    *   **2.6.1** 支持标准的 Spring Security `remember-me` cookie 功能，用于表单登录。


### 2.7 会话管理 (Stateful vs. Stateless)：
    *   **2.7.1 可配置性：**
        *   系统应支持通过配置文件（例如 `application.properties` 或 `application.yml`）切换有状态（Stateful）和无状态（Stateless）模式。
    *   **2.7.2 有状态模式 (Stateful)：**
        *   当配置为有状态时，使用 Spring Session (推荐结合 Redis 或 JDBC 进行会话持久化) 来管理用户会话。
        *   主要目的：实现登录用户的在线统计与管理功能（例如踢出用户、查看在线用户列表）。
        *   在此模式下，Token 可能更多地扮演 CSRF 防护或简化 API 调用的角色，而主要认证状态依赖 Session。
    *   **2.7.3 无状态模式 (Stateless)：**
        *   当配置为无状态时，认证完全依赖于 Token (JWT)。服务器不存储任何 Session 信息。
        *   所有需要认证的请求都必须在 Header 中携带有效的 Token (e.g., `Authorization: Bearer <token>`)。


## 3. 实现方式要求 (非常重要)：

*   **3.1 优先使用 Spring Security 及 OAuth2 组件:**
    *   对于用户名密码认证，利用 `DaoAuthenticationProvider`。
    *   对于 JWT 处理，使用 `JwtEncoder`, `JwtDecoder`。如果需要验证 JWT 签发者、受众等，配置 `JwtAuthenticationProvider` 和 `JwtAuthenticationConverter`。
    *   **对于自定义登录流程 (特别是 JSON 登录和 Token 刷新)，优先考虑配置和扩展现有的 `AuthenticationFilter` (例如通过 `http.addFilterAt/Before/After`)。**
        *   可以参考 `org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer.BearerTokenRequestMatcher` 和 `BearerTokenAuthenticationFilter` 的思路，但用于自定义 Token。
        *   **创建一个继承自 `AbstractAuthenticationProcessingFilter` 的自定义认证 Filter 来处理 JSON 登录请求，并为其配置自定义的 `AuthenticationConverter` 将 JSON 请求体转换为 `UsernamePasswordAuthenticationToken`。**
    *   **Token 认证 (验证 Access Token 的 Filter):**
        *   配置一个 Filter（例如 `BearerTokenAuthenticationFilter`，如果 JWT 符合 Bearer Token 规范）或者一个自定义的 `AuthenticationFilter`（如果需要更特定的逻辑），该 Filter 使用 `AuthenticationConverter` (例如 `JwtAuthenticationConverter` 或自定义Converter) 将请求中的 Token (Header/Cookie) 转换为 `Authentication` 对象。
        *   然后将此 `Authentication` 对象传递给 `AuthenticationManager` (通常是 `ProviderManager`)。
        *   `AuthenticationManager` 再委托给相应的 `AuthenticationProvider` (例如 `JwtAuthenticationProvider` 或自定义的 Provider) 进行验证。
    *   **避免直接继承 `OncePerRequestFilter` 或 `GenericFilterBean` 来从头实现整个登录认证或 Token 认证逻辑，除非上述方案确实无法满足特定需求。**
    *   **避免在 Filter 中直接解析 Token 并调用 `SecurityContextHolder.getContext().setAuthentication(authentication)`；应通过 `AuthenticationManager` -> `AuthenticationProvider` 的流程。**

*   **3.2 AuthenticationConverter 的使用:**
    *   广泛利用自定义的 `AuthenticationConverter` 将不同格式的请求 (JSON body, Form data, Token in header) 转换为相应的 `Authentication` 对象 (如 `UsernamePasswordAuthenticationToken`, `BearerTokenAuthenticationToken` 或自定义 `Authentication` 实现)。

*   **3.3 Token 存储与管理 (Refresh Token):**
    *   Refresh Token 需要持久化存储 (例如，数据库或 Redis)。提供一个简单的接口定义 (`RefreshTokenService`) 和一个基于内存的示例实现 (`InMemoryRefreshTokenService`) 用于演示。

*   **3.4 清晰的配置分离:**
    *   安全配置应集中在 `SecurityFilterChain` Bean 中。
    *   相关的 Beans (如 `UserDetailsService`, `PasswordEncoder`, `JwtEncoder`, `JwtDecoder`, `AuthenticationProvider`s, `AuthenticationConverter`s) 应清晰定义。

## 4. 输出要求：

*   **Java 代码:**
    *   主要的 `SecurityConfig.java` 文件，包含 `SecurityFilterChain` Bean 的配置。
    *   必要的 `AuthenticationConverter` 实现。
    *   必要的 `AuthenticationSuccessHandler` 和 `AuthenticationFailureHandler` 实现 (特别是用于区分 Form 和 JSON 响应)。
    *   `JwtService` 或类似的服务类，用于 JWT 的生成和解析 (封装 `JwtEncoder` 和 `JwtDecoder`)。
    *   `RefreshTokenService` 接口和内存实现。
    *   一个简单的 `UserDetailsService` 实现 (例如 `InMemoryUserDetailsManager`) 用于测试。
    *   如果需要，提供示例 Controller 来演示 JSON 登录端点 (`/api/login`) 和 Token 刷新端点 (`/api/token/refresh`)，如果这些不是完全由 Filter 处理的话。
*   **关键组件的说明:** 简要解释为什么选择特定的组件和设计决策，特别是在遵循“实现方式要求”方面。
*   **项目结构建议:** 组织代码到合适的包下 (e.g., `config`, `security.filters`, `security.handlers`, `security.converters`, `service`, `controller`)。

## 5. 思考与提示：

*   **JSON Login Filter:**
    *   继承 `AbstractAuthenticationProcessingFilter`。
    *   设置 `RequestMatcher` 匹配 JSON 登录路径和 `application/json` Content-Type。
    *   在 `attemptAuthentication` 方法中，使用自定义的 `AuthenticationConverter` 从 `HttpServletRequest` 读取 JSON body 并转换为 `UsernamePasswordAuthenticationToken`。
    *   将此 Token 传递给 `getAuthenticationManager().authenticate()`。
*   **Token Authentication Filter (for subsequent requests):**
    *   可以使用 `BearerTokenAuthenticationFilter` 如果 Access Token 是标准的 Bearer JWT。
    *   如果 Access Token 不是标准 Bearer JWT (例如，自定义 Header 或 Cookie 名称)，则创建一个自定义的 `AuthenticationFilter` (可以参考 `BearerTokenAuthenticationFilter` 结构)，使用 `AuthenticationConverter` 从请求中提取 Token 并转换为 `Authentication` 对象，然后交由 `AuthenticationManager` (配置了 `JwtAuthenticationProvider`) 处理。
*   **Success/Failure Handlers for JSON Login:**
    *   创建自定义的 `AuthenticationSuccessHandler`，在 `onAuthenticationSuccess` 中生成 JWT 和 Refresh Token，并将其写入 JSON 响应。
    *   创建自定义的 `AuthenticationFailureHandler`，在 `onAuthenticationFailure` 中构建 JSON 错误响应。
*   **Refresh Token Endpoint:**
    *   可以是一个 Controller 端点，也可以是一个专用的 Filter。如果用 Controller，确保该端点受保护，但允许携带有效的 Refresh Token (可能需要一个专门的 Filter 来预处理 Refresh Token)。
    *   验证 Refresh Token 的有效性 (是否存在、是否过期、是否被撤销)，然后生成新的 Access Token 和 Refresh Token。
*   **Error Handling:**
    *   配置 `AuthenticationEntryPoint` 处理未认证访问受保护资源的情况 (返回 401 JSON)。
    *   配置 `AccessDeniedHandler` 处理已认证但权限不足的情况 (返回 403 JSON)。

请开始设计并提供代码。
