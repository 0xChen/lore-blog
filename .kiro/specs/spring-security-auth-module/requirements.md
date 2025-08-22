# 需求文档

## 介绍

本文档概述了为 lore-core 项目实现综合性 Spring Security 认证授权模块的需求。该模块将提供多种认证方法、基于令牌的安全机制、会话管理和灵活的配置选项，同时最大化利用 Spring Security 的原生组件和扩展点。

## 需求

### 需求 1: 多种认证方式支持

**用户故事:** 作为开发者，我希望同时支持传统的表单登录和现代的 API JSON 登录，以便系统能够有效地为 Web 应用程序和 API 客户端提供服务。

#### 验收标准

1. 当用户向 `/login` 提交 `application/x-www-form-urlencoded` 类型的表单登录请求时，系统应使用标准 HTML 表单提交进行认证，并启用 CSRF 保护
2. 当用户向 `/api/login` 提交包含用户名和密码字段的 `application/json` 类型的 API 登录请求时，系统应使用 JSON 载荷进行认证，并禁用 CSRF 保护
3. 当使用任一认证方法时，系统应利用 Spring Security 的原生 `AuthenticationProvider` 和 `AuthenticationConverter` 组件
4. 当处理认证请求时，系统应使用 `DelegatingAuthenticationConverter` 来适当处理不同的请求类型

### 需求 2: 动态令牌生成和管理

**用户故事:** 作为 API 客户端，我希望根据请求偏好接收 JWT 或 UUID 令牌，以便为我的用例选择最合适的令牌类型。

#### 验收标准

1. 当认证成功时，系统应根据客户端请求头（Accept 头或自定义参数）生成令牌
2. 当请求 JWT 令牌类型时，系统应使用 Spring Security 的 `JwtEncoder` 生成标准 JWT 访问令牌
3. 当请求 UUID 令牌类型时，系统应生成基于 UUID 的不透明访问令牌
4. 当任何认证成功时，系统应始终生成安全的刷新令牌（UUID 或随机字符串）
5. 当生成令牌时，访问令牌过期时间应可通过 `application.yml` 配置，默认为 30 分钟
6. 当发起后续请求时，访问令牌应同时支持 HTTP 头和 URL 参数携带

### 需求 3: 令牌生命周期和刷新机制

**用户故事:** 作为客户端应用程序，我希望使用刷新令牌来刷新过期的访问令牌，以便用户无需频繁重新认证。

#### 验收标准

1. 当使用过期的访问令牌时，系统应返回 HTTP 401 未授权状态码和 JSON 错误响应
2. 当向 `/api/token/refresh` 发起包含有效 refresh_token 的刷新请求时，系统应生成新的访问令牌和刷新令牌
3. 当处理刷新令牌请求时，系统应同时支持 `application/json` 和 `application/x-www-form-urlencoded` 内容类型
4. 当访问刷新端点时，该端点应禁用 CSRF 保护
5. 当刷新令牌无效或过期时，系统应返回适当的 JSON 错误响应

### 需求 4: 差异化响应处理

**用户故事:** 作为系统集成者，我希望为基于表单和基于 API 的认证提供不同的响应格式，以便每种客户端类型都能收到适当的响应。

#### 验收标准

1. 当表单登录成功时，系统应使用 `SavedRequestAwareAuthenticationSuccessHandler` 行为重定向到配置的目标 URL
2. 当表单登录失败时，系统应重定向到配置的失败 URL 并携带错误参数
3. 当 API 登录成功时，系统应返回 HTTP 200 状态码和包含 access_token、refresh_token、token_type 和 expires_in 的 JSON 响应
4. 当 API 登录失败时，系统应返回 HTTP 401 状态码和 JSON 错误响应
5. 当配置响应处理时，系统应使用 `DelegatingAuthenticationSuccessHandler` 和 `DelegatingAuthenticationFailureHandler` 根据请求类型进行路由

### 需求 5: 记住我功能

**用户故事:** 作为 Web 应用程序用户，当我勾选"记住我"时，我希望在浏览器会话间保持登录状态，以便无需重复登录。

#### 验收标准

1. 当在表单登录期间选择记住我复选框时，系统应创建持久的记住我 Cookie
2. 当进行记住我认证时，系统应使用 Spring Security 的 `RememberMeServices`（基于令牌或持久令牌）
3. 当记住我认证成功时，系统应在有状态模式下建立用户会话
4. 当处理记住我令牌时，系统应与现有认证流程集成

### 需求 6: 混合会话管理

**用户故事:** 作为系统管理员，我希望同时支持有状态和无状态模式，以便在高效支持 API 客户端的同时管理在线用户。

#### 验收标准

1. 当启用有状态模式时，系统应支持在线用户管理（统计、查看、会话终止）
2. 当使用有状态会话时，系统应使用 Redis 或 JDBC 持久化与 Spring Session 集成
3. 当使用无状态模式时，系统应纯粹依赖基于令牌的认证
4. 当配置会话管理时，两种模式应能共存并可根据请求上下文进行选择

### 需求 7: 原生组件集成

**用户故事:** 作为开发者，我希望最大化使用 Spring Security 的原生组件，以便实现可维护且遵循框架最佳实践。

#### 验收标准

1. 当实现认证提供者时，系统应使用 `DaoAuthenticationProvider` 处理用户名/密码认证，使用 `JwtAuthenticationProvider` 处理 JWT 令牌
2. 当实现令牌处理时，系统应使用 `JwtEncoder`、`JwtDecoder` 和 `BearerTokenAuthenticationConverter`
3. 当实现自定义认证时，系统应扩展 `AbstractAuthenticationProcessingFilter` 而不是 `OncePerRequestFilter`
4. 当配置认证流程时，系统不应手动操作 `SecurityContextHolder`，而应依赖 `AuthenticationManager` 和 `AuthenticationProvider`
5. 当实现不透明令牌认证时，系统应创建与标准认证流程集成的自定义 `AuthenticationProvider`

### 需求 8: 配置和属性管理

**用户故事:** 作为 DevOps 工程师，我希望有集中化和类型安全的配置管理，以便安全设置能够轻松维护和验证。

#### 验收标准

1. 当定义配置时，系统应使用 `@ConfigurationProperties` 类而不是分散的 `@Value` 注解
2. 当管理安全属性时，系统应提供带有 `@EnableConfigurationProperties` 的 `SecurityProperties` 类
3. 当配置安全过滤器链时，系统应使用清晰的 `SecurityFilterChain` Bean 分离，避免复杂的条件逻辑
4. 当定义依赖项时，服务、提供者、转换器和处理器应定义为独立、专注的 Bean

### 需求 9: 令牌存储和服务接口

**用户故事:** 作为开发者，我希望有定义良好的令牌管理服务接口，以便不同的存储实现可以轻松替换。

#### 验收标准

1. 当实现令牌服务时，系统应提供包含存储、验证和删除操作的 `TokenService` 接口
2. 当实现刷新令牌服务时，系统应提供具有全面令牌生命周期管理的 `RefreshTokenService` 接口
3. 当需要演示实现时，系统应提供 `InMemoryRefreshTokenService` 作为参考实现
4. 当进行生产部署时，接口应支持数据库或 Redis 实现

### 需求 10: 现代 Java 特性和最佳实践

**用户故事:** 作为开发者，我希望适当使用现代 Java 特性，以便代码简洁可维护，同时与 Spring Framework 保持兼容。

#### 验收标准

1. 当实现组件时，系统应在不影响 Spring 兼容性的前提下使用 JDK 17+ 特性来提高代码清晰度
2. 当组织代码时，系统应使用逻辑清晰的包结构和单数命名（config、filter、handler、converter、provider、service、properties、user）
3. 当添加注释时，系统应使用简洁的中文注释，专注于关键设计决策而非显而易见的代码解释
4. 当使用请求匹配时，系统应利用 `RequestMatcher` 实现来精确应用安全规则