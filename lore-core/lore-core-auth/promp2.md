作为一位资深的 Spring Security 架构师，请根据以下需求设计并实现一个基于 Spring Boot 的安全认证授权模块。

**项目核心依赖版本：**
*   Spring Boot: [请指定你希望的 Spring Boot 版本，例如 3.2.x]
*   Spring Security: 6.4.5 (最低也需保证 6.0+ 版本)
*   Java: [请指定你希望的 Java 版本，例如 17 或 21]
*   构建工具: Maven 或 Gradle [请选择一个]

**功能需求详解：**

1.  **多种密码提交方式支持：**
    *   **1.1 表单登录：**
        *   支持传统的 HTML 表单提交方式 ( `application/x-www-form-urlencoded` ) 进行用户名密码登录。
        *   登录端点：例如 `/login` (可配置)。
    *   **1.2 JSON 登录：**
        *   支持通过 POST 请求，以 JSON 数据格式 ( `application/json` ) 提交用户名和密码进行登录。
        *   登录端点：例如 `/api/login` (可配置，且应与表单登录端点区分或通过 `Content-Type` 区分处理)。

2.  **账号密码认证成功后处理：**
    *   **2.1 Token 颁发：**
        *   登录成功后，需返回 Token。
        *   Token 格式：优先考虑 JWT (JSON Web Token)。如果实现 JWT 复杂，可以考虑返回一个安全的、唯一的、类似 UUID 的 opaque token（不透明令牌），但请优先推荐并实现 JWT 方案。
        *   如果使用 JWT，Token 中应包含用户标识、权限等必要信息。

3.  **Token 过期机制：**
    *   **3.1 过期时间配置：**
        *   Access Token 的过期时间应支持在配置文件中配置，默认值为 30 分钟。
    *   **3.2 过期响应：**
        *   Access Token 过期后，访问受保护资源时，应返回 HTTP 401 Unauthorized 错误码。
    *   **3.3 刷新提示：**
        *   返回 401 错误码时，响应体中可以包含特定错误信息或错误码，以提示前端可以使用 Refresh Token 进行刷新。

4.  **Token 刷新机制：**
    *   **4.1 Refresh Token：**
        *   在用户首次登录成功时，除了颁发 Access Token，还应颁发一个 Refresh Token。
        *   Refresh Token 具有比 Access Token 更长的有效期（例如 7 天，可配置）。
        *   Refresh Token 应被安全存储（例如，如果 Access Token 在客户端 LocalStorage，Refresh Token 可以在 HttpOnly Cookie 中，或有其他安全存储机制）。
    *   **4.2 刷新逻辑：**
        *   提供一个专门的刷新端点（例如 `/api/token/refresh`）。
        *   前端使用有效的 Refresh Token 请求该端点。
        *   后端验证 Refresh Token 的有效性（未过期、未被吊销等）。
        *   验证通过后，颁发新的 Access Token 和（可选，但推荐）新的 Refresh Token。

5.  **登录成功/失败响应处理：**
    *   **5.1 表单登录：**
        *   成功：重定向到指定的成功 URL（例如 `/home`）。
        *   失败：重定向到指定的失败 URL（例如 `/login?error`）。
        *   此逻辑应类似于 Spring Security 中 `AbstractAuthenticationTargetUrlRequestHandler` 及其子类（如 `SimpleUrlAuthenticationSuccessHandler`, `SimpleUrlAuthenticationFailureHandler`）的行为。
    *   **5.2 JSON 登录：**
        *   成功：返回 JSON 格式的响应，包含：
            *   `access_token`: 新的 Access Token
            *   `refresh_token`: Refresh Token
            *   `token_type`: "Bearer"
            *   `expires_in`: Access Token 有效期 (秒)
            *   用户信息（可选，如用户ID、用户名）
        *   失败：返回 JSON 格式的响应，包含错误码和错误信息（例如 `{"error": "invalid_credentials", "error_description": "Bad credentials"}`）。

6.  **基于 Cookie 的“记住我”功能：**
    *   **6.1 Remember-Me Cookie：**
        *   支持标准的 Spring Security "remember-me" 功能。
        *   用户在表单登录时勾选“记住我”，在 Session 过期或浏览器关闭后重新访问时，能够自动认证。
        *   需要配置 `RememberMeServices`。

7.  **会话管理 (Stateful vs. Stateless)：**
    *   **7.1 可配置性：**
        *   系统应支持通过配置文件（例如 `application.properties` 或 `application.yml`）切换有状态（Stateful）和无状态（Stateless）模式。
    *   **7.2 有状态模式 (Stateful)：**
        *   当配置为有状态时，使用 Spring Session (推荐结合 Redis 或 JDBC 进行会话持久化) 来管理用户会话。
        *   主要目的：实现登录用户的在线统计与管理功能（例如踢出用户、查看在线用户列表）。
        *   在此模式下，Token 可能更多地扮演 CSRF 防护或简化 API 调用的角色，而主要认证状态依赖 Session。
    *   **7.3 无状态模式 (Stateless)：**
        *   当配置为无状态时，认证完全依赖于 Token (JWT)。服务器不存储任何 Session 信息。
        *   所有需要认证的请求都必须在 Header 中携带有效的 Token (e.g., `Authorization: Bearer <token>`)。

**实现方式与架构要求 (非常重要)：**

1.  **组件利用：**
    *   **优先方案：** 尽可能地利用 Spring Security 及 Spring Security OAuth2 (如果引入 JWT 相关) 中的现有组件和设计模式。
        *   例如，对于 JWT 认证，考虑使用 `JwtAuthenticationProvider`、`JwtDecoder`、`JwtEncoder` 以及 `BearerTokenAuthenticationConverter` (或自定义的 `AuthenticationConverter`)。
        *   对于登录处理，优先配置和使用 Spring Security 内建的 `AuthenticationFilter`（如 `UsernamePasswordAuthenticationFilter` 的现代替代品或通过 `HttpSecurity` 的 DSL 配置），并为其提供自定义的 `AuthenticationConverter` (将请求转换为 `Authentication` 对象) 和 `AuthenticationManager`。
        *   可以参考 Spring 官方 `OneTimeTokenLoginConfigurer` 中使用 `AuthenticationFilter(authenticationManager, this.authenticationConverter)` 的方式，而不是直接创建一个新的 `OneTimeTokenAuthenticationFilter`。
        *   或者，如果需要更复杂的登录流程控制，可以考虑创建继承自 `AbstractAuthenticationProcessingFilter` 的自定义 `AuthenticationFilter`。
    *   **次选方案 (尽量避免用于核心登录认证)：** 只有在上述方案确实无法满足需求时，才考虑直接继承 `OncePerRequestFilter` 或 `GenericFilterBean` 来创建全新的 Filter 实现登录认证逻辑。

2.  **认证流程：**
    *   **推荐流程：** 尽量利用已有的或自定义的 `AuthenticationConverter` 将不同格式的请求（表单、JSON、Token）转换成其对应的 `Authentication` 对象。
    *   然后将此 `Authentication` 对象传递给 `AuthenticationManager` 的实现类（可以是系统提供的，或者在必要时自定义实现的）。
    *   `AuthenticationManager` 再利用系统已有的或自定义的 `AuthenticationProvider` (例如 `DaoAuthenticationProvider` 处理用户名密码，`JwtAuthenticationProvider` 处理 JWT) 来完成实际的认证逻辑。
    *   **避免直接操作 `SecurityContextHolder`：** 避免在 Filter 中直接解析 Token 或请求参数，然后手动创建 `Authentication` 对象并调用 `SecurityContextHolder.getContext().setAuthentication(authentication)`。应将此职责交给 `AuthenticationManager` 和 `AuthenticationProvider`。

**产出要求：**

1.  **Java 配置类：** 提供核心的 Spring Security 配置类 (`@Configuration` 类)。
2.  **关键组件代码：**
    *   自定义的 `AuthenticationConverter` (如果需要)。
    *   自定义的 `AuthenticationProvider` (如果需要)。
    *   Token 生成和解析服务 (例如 JWT Service)。
    *   Refresh Token 存储和管理相关逻辑 (如果 Refresh Token 需要持久化)。
    *   处理 JSON 登录和表单登录的 `AuthenticationSuccessHandler` 和 `AuthenticationFailureHandler` 的定制化实现。
3.  **配置文件示例：** 相关的 `application.properties` 或 `application.yml` 配置项。
4.  **关键设计思路解释：** 对重要的设计决策和组件选择进行简要说明。
5.  **代码结构建议：** 给出推荐的包结构。

**请确保你的设计具有良好的可扩展性和可维护性，并遵循 Spring Security 的最佳实践。如果任何需求点存在冲突或不明确之处，请指出并提出你的建议。**
