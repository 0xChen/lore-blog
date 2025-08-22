下面提示词写的代码： https://aistudio.google.com/prompts/1BQr-RUVmoftwup-UKyWs0ZZc3nwkz9UV

# 角色
你是一位精通 Spring Security 6.5.+ 和 Spring Security OAuth2 的资深架构师，对 Spring Boot 生态、认证授权机制、RESTFUL API 设计以及有状态/无状态架构有深入理解和丰富的实战经验。

# 任务
根据以下详细需求，设计并提供一个基于 Spring Boot (使用 3.5.0 版本) 和 Spring Security 6.5.0 的安全认证授权模块的核心配置代码和关键组件实现。
首要目标是最大限度地利用 Spring Security 及 Spring Security OAuth2 的原生组件和扩展点 (如 `AuthenticationProvider`, `AuthenticationConverter`, `AuthenticationSuccessHandler`, `AuthenticationFilter` 的配置等)，
避免直接继承 `OncePerRequestFilter` 或 `GenericFilterBean` 创建全新的认证 Filter，除非原生机制确实无法满足需求。

# 需求详情

## 1. 版本要求
- Spring Boot: 3.5.0 或更高版本
- Spring Security: 与所选 Spring Boot 版本兼容的最新稳定版，当前最新版本是6.5.0

## 2. 功能描述

### 2.1 多种登录方式支持
- 2.1.1 表单登录 (Form Login):
    - 标准 HTML 表单提交 (`application/x-www-form-urlencoded`)。
    - 路径: `/login` (POST)。
    - 需要支持标准的 CSRF 防护。
- 2.1.2 API JSON 登录 (API Login):
    - POST 请求，`Content-Type: application/json`。
    - 请求体包含 `username` 和 `password` 字段。
    - 路径: `/api/login` (POST)。
    - 此路径应禁用 CSRF 防护。


### 2.2 认证成功后的 Token 颁发
- 2.2.1 动态 Token 类型:
    - 认证成功后，根据客户端请求指定的方式 (例如，通过请求头 `Accept` 或自定义请求参数/头)，动态决定返回以下两种 Token 类型之一：
        - JWT: 生成标准的 JWT Access Token。
        - UUID (Opaque Token): 生成一个 UUID 作为 Access Token (代表一个服务器端存储的关联状态，可能是匿名会话标识或与 Refresh Token 关联)。
    - Refresh Token: 始终生成一个安全的、通常是 Opaque 的字符串（例如 UUID 或更长的随机串），用于刷新 Access Token。
    - 响应格式: 无论是哪种登录方式成功，都需要根据请求类型返回相应格式的 Token 信息。
- 2.2.2 认证成功后:
    - 用户名和密码在提交认证成功后，后续的请求将使用 Access Token 进行认证，Access Token可能在 header 或者 url 的参数中携带。


### 2.3 Token 属性与处理
- 2.3.1 Access Token 过期时间:
    - 可通过配置文件 (`application.yml`) 配置，默认 30 分钟。
- 2.3.2 Access Token 过期处理:
    - 携带过期 Access Token 访问受保护资源时，返回 HTTP 401 Unauthorized 错误码。
    - 响应体应为 JSON 格式，包含明确的错误信息 (例如 `{"status": 401, "error": "Unauthorized", "message": "Access token expired"}`)。
- 2.3.3 Refresh Token 使用:
    - 前端应能使用 Refresh Token 调用刷新接口获取新的 Access Token。

### 2.4 Token 刷新机制
- 2.4.1 刷新端点:
    - `/api/token/refresh` (POST)。
    - 请求体包含 `refresh_token` 字段 (`Content-Type: application/json` 或 `application/x-www-form-urlencoded` 均可考虑支持)。
    - 此端点应禁用 CSRF 防护（如果需要支持 Form 方式提交）。
- 2.4.2 刷新成功:
    - 返回新的 Access Token (类型与原 Token 保持一致或可配置) 和新的 Refresh Token (推荐刷新以提高安全性)。
    - 响应格式为 JSON，包含新的 token 信息。
- 2.4.3 刷新失败/无效:
    - 返回 HTTP 401 Unauthorized 或 403 Forbidden 错误码。
    - 响应体为 JSON 格式，包含错误信息 (例如 `{"status": 401, "error": "Unauthorized", "message": "Invalid refresh token"}`)。

### 2.5 登录成功/失败响应处理 (区分请求类型)
- 2.5.1 表单登录 (Form Login):
    - 成功: 重定向到配置的目标 URL。行为类似于 `SavedRequestAwareAuthenticationSuccessHandler` 或 `SimpleUrlAuthenticationSuccessHandler`。
    - 失败: 重定向到配置的失败 URL，并携带错误信息参数。行为类似于 `SimpleUrlAuthenticationFailureHandler`。
- 2.5.2 API JSON 登录 (API Login):
    - 成功: 返回 HTTP 200 OK，响应体为 JSON 格式，包含:
      ```json
      {
        "access_token": "...", // JWT 或 UUID
        "refresh_token": "...", // Opaque Refresh Token
        "token_type": "Bearer", // 或其他合适的类型
        "expires_in": 1800 // Access Token 剩余有效时间 (秒)
      }
      ```
    - 失败: 返回 HTTP 401 Unauthorized，响应体为 JSON 格式，包含:
      ```json
      {
        "timestamp": "...",
        "status": 401,
        "error": "Unauthorized",
        "message": "Invalid credentials" // 或具体的失败原因
      }
      ```

### 2.6 基于 Cookie 的记住我功能 (Remember-Me)
- 2.6.1 Remember-Me Cookie:
    - 支持标准的 Spring Security "remember-me" 功能，通过表单登录界面的复选框触发。
    - 需要配置 `RememberMeServices` (例如 `TokenBasedRememberMeServices` 或 `PersistentTokenBasedRememberMeServices`)。
    - Remember-Me 认证成功后，应建立用户会话（尤其是在有状态模式下）。

### 2.7 会话管理: 同时支持有状态 (Stateful) 与无状态 (Stateless)
- 2.7.1 有状态模式 (Stateful):
    - 目的: 主要为了实现在线用户管理（统计、查看、踢出等）。
    - 实现:
        - 启用 Spring Session (推荐结合 Redis 或 JDBC 进行会话持久s化)。
        - Session 创建策略: `SessionCreationPolicy.IF_REQUIRED` (默认)。
        - 交互:
            - 对于浏览器/表单登录，认证成功后创建并依赖 HttpSession。Remember-Me 也会恢复 Session。
            - API 请求（携带 Token）在此模式下如何处理？
                - 方案A (推荐): Token 主要用于 API 认证。如果请求携带有效 Token，则认证该 Token 并处理请求，不创建 Session。如果请求没有 Token 但有有效 Session Cookie，则使用 Session。这需要配置不同的 SecurityMatcher 应用不同策略。
                - 方案B: Token 仅作为访问 API 的凭证，服务器仍查找或创建 Session。
                - 请优先考虑并实现方案A，确保 Session 和 Token 认证可以共存但应用于不同场景/请求类型。
- 2.7.2 无状态模式 (Stateless):
    - 实现:
        - Session 创建策略: `SessionCreationPolicy.STATELESS`。
        - 服务器不创建或使用 HttpSession。
        - 所有需要认证的请求 (Web UI 或 API) 都必须携带有效的 Access Token (通常在 `Authorization: Bearer <token>` Header 中)。
        - Remember-Me 功能在此模式下通常不适用或需要特殊处理（因为它传统上依赖 Session）。如果需要类似效果，需完全基于 Token 实现。请明确在此模式下 Remember-Me 的行为 (默认禁用或有特殊实现)。

## 3. 实现方式核心要求 (!!! 非常重要 !!!)

### 3.1 优先使用原生组件和扩展点
- 认证提供者:
    - 用户名密码认证: 使用 `DaoAuthenticationProvider`，配置 `UserDetailsService` 和 `PasswordEncoder`。
    - JWT Token 认证: 配置 `JwtAuthenticationProvider`，需要 `JwtDecoder`。
    - Opaque Token (UUID) 认证: 可能需要自定义 `AuthenticationProvider` 来验证 Opaque Token (例如，查询关联的 Refresh Token 或 Session 存储)。
- JWT 处理: 使用 `JwtEncoder`, `JwtDecoder` Bean。
- 认证转换器 (`AuthenticationConverter`):
    - 必须使用 `org.springframework.security.web.authentication.AuthenticationConverter` 接口的实现。
    - 强烈推荐使用 `DelegatingAuthenticationConverter` 来组合处理不同类型的认证请求：
        - 一个 `AuthenticationConverter` 用于将 Form Login 请求 (`HttpServletRequest`) 转换为 `UsernamePasswordAuthenticationToken`。
        - 一个 `AuthenticationConverter` 用于将 API Login JSON 请求 (`HttpServletRequest`) 读取 Body 并转换为 `UsernamePasswordAuthenticationToken`。
        - 一个 `AuthenticationConverter` (例如 `BearerTokenAuthenticationConverter` 或自定义实现) 用于将请求 Header/Parameter 中的 Access Token (JWT 或 Opaque) 转换为相应的 `Authentication` 对象 (如 `BearerTokenAuthenticationToken` 或自定义类型)。
- 认证过滤器 (`AuthenticationFilter`):
    - 对于 API JSON 登录:
        - 首选方案: 创建一个继承自 `AbstractAuthenticationProcessingFilter` 的自定义 Filter。配置其 `requestMatcher` 匹配 `/api/login` 和 `application/json`。在其 `attemptAuthentication` 方法中使用上面定义的 JSON `AuthenticationConverter`。
        - 备选方案 (如需更灵活控制): 使用 `org.springframework.security.web.authentication.AuthenticationFilter` 并为其注入 `AuthenticationManager` 和自定义的 JSON `AuthenticationConverter`。
    - 对于 Token 认证 (验证后续请求中的 Access Token):
        - 首选方案 (JWT): 配置 `OAuth2ResourceServerConfigurer` 并使用 `jwt()` DSL，这将自动配置 `BearerTokenAuthenticationFilter`、`JwtAuthenticationProvider` 等。
        - 首选方案 (Opaque Token 或混合): 使用通用的 `org.springframework.security.web.authentication.AuthenticationFilter`，为其注入 `AuthenticationManager` 和上面定义的 Token `AuthenticationConverter` (能处理 JWT 和 Opaque)。`AuthenticationManager` 需要配置相应的 `AuthenticationProvider` (Jwt 和 Opaque 的)。
    - 绝对避免: 直接继承 `OncePerRequestFilter` 或 `GenericFilterBean` 来编写完整的、手动的认证逻辑 (包括解析 Token、设置 `SecurityContext`)。认证流程必须经过 `AuthenticationConverter -> AuthenticationFilter -> AuthenticationManager -> AuthenticationProvider`。
- 禁止直接操作 `SecurityContextHolder`: 不要在 Filter 中手动解析凭证并调用 `SecurityContextHolder.getContext().setAuthentication()`，让 `AuthenticationManager` 和 `AuthenticationProvider` 完成认证和填充 `SecurityContext` 的工作。

### 3.2 成功/失败处理器 (`AuthenticationSuccessHandler` / `AuthenticationFailureHandler`)
- 区分处理:
    - 使用 `DelegatingAuthenticationSuccessHandler` 和 `DelegatingAuthenticationFailureHandler` (如果适用，或者自定义分发逻辑)。
    - 根据请求类型 (Form vs API) 路由到不同的处理器实现：
        - Form Login: 使用 Spring 内置的 `SimpleUrlAuthenticationSuccessHandler`/`SimpleUrlAuthenticationFailureHandler` 或其子类进行重定向。
        - API Login: 实现自定义的 `AuthenticationSuccessHandler` (生成 Token 并返回 JSON) 和 `AuthenticationFailureHandler` (返回 JSON 错误)。可参考 `HttpMessageConverter` 相关 Handler 的实现思路，使用 ObjectMapper 写 JSON 响应。
- 动态 Token 生成: API 登录的 `AuthenticationSuccessHandler` 需要包含根据客户端请求生成 JWT 或 UUID Token 的逻辑。

### 3.3 Token 存储与管理
- 提供 `TokenService` 接口定义，包含存储、验证、删除 Generate Token 的方法。


### 3.4 Refresh Token 存储与管理
- 提供 `RefreshTokenService` 接口定义，包含存储、验证、删除 Refresh Token 的方法。
- 提供一个基于内存的示例实现 (`InMemoryRefreshTokenService`) 用于演示。在实际应用中，应替换为数据库或 Redis 实现。

### 3.5 配置方式
- 配置类: 使用 `@Configuration` 类来组织 Spring Security 的 Bean 定义。
- 属性注入: 必须使用 `@ConfigurationProperties` 创建属性类 (e.g., `SecurityProperties.java`)，并通过 `@EnableConfigurationProperties` 启用，禁止使用 `@Value` 注解分散地注入配置项。
- 清晰分离: 使用更清晰的 `SecurityFilterChain` Bean分层策略，避免复杂的条件判断逻辑，依赖的其他 Bean (Services, Providers, Converters, Handlers, Encoder/Decoder) 应单独定义。

### 3.6 Java 版本特性
- 在不影响代码可读性、可维护性和 Spring 框架兼容性的前提下，适度使用 JDK 17+ (直至目标 JDK 版本 24) 的新特性和语法糖 (例如 `record`, `var`, `switch` 表达式, `text blocks`) 来提高代码简洁性。

## 4. 输出要求
- Java 代码:
    - `SecurityConfig.java`: 包含核心 `SecurityFilterChain` Bean 配置，使用多个` @Order` 的 `SecurityFilterChain` Bean 职责分离，同时支持有状态登录和无状态模式。
    - `TokenConfig.java`: 配置 `JwtEncoder`, `JwtDecoder`, 相关属性。
    - `AuthenticationConverter` 实现 (Form, JSON API, Token)。
    - `AuthenticationSuccessHandler` / `AuthenticationFailureHandler` 实现 (API JSON 返回)。
    - `TokenService` 接口和其典型的实现类`JwtTokenService.java` / `OpaqueTokenService.java` 封装 Token 生成逻辑。
    - `RefreshTokenService.java` (接口) 和 `InMemoryRefreshTokenService.java` (实现)。
    - `UserDetailsServiceImpl.java`: 一个简单的 `UserDetailsService` 实现 (使用内存用户)。
    - `SecurityProperties.java`: 使用 `@ConfigurationProperties` 收集所有安全相关配置。
    - `ApiLoginAuthenticationFilter.java` (如果选择继承 `AbstractAuthenticationProcessingFilter`)。
    - `Token` 刷新逻辑使用 `Filter` 体系实现。
    -  示例 `Controller` 演示受保护资源访问。
- 配置文件:
    - `application.yml` 包含切换 Token 过期时间、JWT 密钥（或生成方式说明）等配置。
- 关键组件说明:
    - 对核心设计决策进行简要注释或说明，特别是如何遵循“实现方式核心要求”的部分。解释为什么选择特定的原生组件组合。
- 项目结构建议:
    - 使用逻辑清晰的包结构 (e.g., `com.developerchen.core.auth.config`, `com.developerchen.core.auth.security.filter`, `com.developerchen.core.auth.security.handler`, `com.developerchen.core.auth.security.converter`, `com.developerchen.core.auth.security.provider`, `com.developerchen.core.auth.security.service`, `com.developerchen.core.auth.security.properties`, `com.developerchen.core.auth.user`)。包名推荐使用单数形式。
- 代码注释的建议:
    - 代码中使用中文注释，另外适当精简注释，保留关键注释。例如下面的注释就很多余属于废话无意义。 @Bean // Bean for our custom provider，避免重复代码本身表达的内容，

## 4. 构建工具要求
- Gradle: 创建一个 Gradle 项目。


## 6. 思考与提示
- Request Matchers: 充分利用 `RequestMatcher` (如 `AntPathRequestMatcher`, `MediaTypeRequestMatcher`, `AndRequestMatcher`, `OrRequestMatcher`) 来精确地将不同的安全规则、Filter、Handler、EntryPoint 应用到不同的请求路径和类型上。这是区分 Form/API/Stateless/Stateful 行为的关键。

- Token 认证 Filter (`AuthenticationFilter`): 需要配置好 `setAuthenticationConverter`, `setSuccessHandler` (通常是 NoOp 或继续 FilterChain), `setFailureHandler` (通常委托给 `AuthenticationEntryPoint`)。
- 错误处理 (`AuthenticationEntryPoint` / `AccessDeniedHandler`): 配置能够根据请求类型返回不同响应 (重定向 vs JSON) 的实现，例如使用 `DelegatingAuthenticationEntryPoint` 或根据 `RequestMatcher` 选择不同的 EntryPoint。
- Refresh Token 端点: 实现时要考虑安全性，验证 Refresh Token 的有效性（是否存在、未过期、未被撤销）。这个端点本身也需要安全配置（可能允许匿名访问，但内部逻辑会验证 Token）。

请基于以上要求，提供高质量、可复用、遵循最佳实践的代码和配置。如果任何需求存在冲突或实现难点，请明确指出并提出建议的替代方案。
在开始前，你需要先调用MCP工具中的顺序思维工具(context7)，对设计进行梳理与规划获取最新 doc，请开始设计并提供代码。
