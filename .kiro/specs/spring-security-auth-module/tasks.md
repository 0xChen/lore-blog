# 实现计划

- [x] 1. 项目结构和核心配置设置
  - 创建包结构和基础配置类
  - 配置 Gradle 依赖项和 Spring Boot 版本
  - 设置配置属性类和基础 Bean 定义
  - _需求: 8.1, 8.3, 10.2_

- [x] 2. 配置属性和基础服务接口
- [x] 2.1 实现 SecurityProperties 配置类
  - 创建 `SecurityProperties` 类，包含令牌、会话和记住我配置
  - 使用 `@ConfigurationProperties` 注解和类型安全的属性绑定
  - 编写配置属性的单元测试
  - _需求: 8.1, 8.2_

- [x] 2.2 定义令牌服务接口
  - 创建 `TokenService` 接口，包含存储、验证、删除操作
  - 创建 `RefreshTokenService` 接口，包含刷新令牌生命周期管理
  - 定义相关的数据模型类（TokenType、TokenValidationResult、TokenPair）
  - _需求: 9.1, 9.2_

- [x] 2.3 实现内存版本的令牌服务
  - 实现 `InMemoryTokenService` 作为 TokenService 的演示实现
  - 实现 `InMemoryRefreshTokenService` 作为参考实现
  - 编写服务类的单元测试
  - _需求: 9.3_

- [x] 3. 认证转换器实现
- [x] 3.1 实现表单认证转换器
  - 创建 `FormAuthenticationConverter` 处理标准表单登录请求
  - 实现请求匹配逻辑，仅处理 `/login` 路径的表单提交
  - 编写转换器的单元测试
  - _需求: 1.1, 1.3_

- [x] 3.2 实现 API 认证转换器
  - 创建 `ApiAuthenticationConverter` 解析 JSON 请求体
  - 实现 JSON 解析逻辑，提取 username 和 password 字段
  - 添加请求匹配逻辑，仅处理 `/api/login` 路径和 `application/json` 内容类型
  - 编写转换器的单元测试，包括边界条件测试
  - _需求: 1.2, 1.3_

- [x] 3.3 实现令牌认证转换器
  - 创建 `TokenAuthenticationConverter` 从请求中提取访问令牌
  - 支持从 HTTP 头和 URL 参数中提取令牌
  - 根据令牌格式创建相应的 Authentication 对象（JWT 或不透明令牌）
  - 编写转换器的单元测试
  - _需求: 2.6, 7.2_

- [x] 3.4 实现委托认证转换器
  - 创建 `DelegatingAuthenticationConverter` 组合多个转换器
  - 实现转换器的优先级和匹配逻辑
  - 编写集成测试验证转换器委托行为
  - _需求: 1.4_

- [x] 4. 自定义认证提供者实现
- [x] 4.1 实现不透明令牌认证提供者
  - 创建 `OpaqueTokenAuthenticationProvider` 验证 UUID 类型令牌
  - 集成 TokenService 进行令牌验证和用户信息获取
  - 创建 `OpaqueTokenAuthenticationToken` 自定义认证对象
  - 编写提供者的单元测试
  - _需求: 7.5, 2.3_

- [x] 4.2 配置 JWT 认证提供者
  - 配置 `JwtAuthenticationProvider` 和相关的 `JwtDecoder`
  - 设置 JWT 签名验证和过期时间检查
  - 编写 JWT 认证的集成测试
  - _需求: 7.1, 2.2_

- [x] 5. 认证成功和失败处理器
- [x] 5.1 实现 API 认证成功处理器
  - 创建 `ApiAuthenticationSuccessHandler` 生成令牌响应
  - 实现动态令牌类型选择逻辑（JWT 或 UUID）
  - 集成 TokenService 和 RefreshTokenService 生成令牌
  - 返回标准 JSON 格式的令牌响应
  - 编写处理器的单元测试
  - _需求: 4.3, 2.1, 2.4_

- [x] 5.2 实现表单认证成功处理器
  - 配置 `FormAuthenticationSuccessHandler` 进行重定向处理
  - 使用 Spring Security 的 `SavedRequestAwareAuthenticationSuccessHandler`
  - 编写表单登录成功的集成测试
  - _需求: 4.1_

- [x] 5.3 实现认证失败处理器
  - 创建 `ApiAuthenticationFailureHandler` 返回 JSON 错误响应
  - 创建 `FormAuthenticationFailureHandler` 进行错误重定向
  - 实现 `DelegatingAuthenticationFailureHandler` 根据请求类型路由
  - 编写失败处理器的单元测试
  - _需求: 4.2, 4.4, 4.5_

- [x] 5.4 实现委托认证成功处理器
  - 创建 `DelegatingAuthenticationSuccessHandler` 根据请求类型路由
  - 实现请求类型识别逻辑（表单 vs API）
  - 编写委托处理器的集成测试
  - _需求: 4.5_

- [x] 6. 认证过滤器配置
- [x] 6.1 实现 API 登录认证过滤器
  - 创建继承自 `AbstractAuthenticationProcessingFilter` 的 `ApiLoginAuthenticationFilter`
  - 配置请求匹配器匹配 `/api/login` 和 JSON 内容类型
  - 集成 ApiAuthenticationConverter 和相应的成功/失败处理器
  - 编写过滤器的集成测试
  - _需求: 7.3_

- [x] 6.2 配置令牌认证过滤器
  - 使用 `AuthenticationFilter` 配置令牌认证
  - 集成 TokenAuthenticationConverter 和 AuthenticationManager
  - 配置适当的成功和失败处理器
  - 编写令牌认证的集成测试
  - _需求: 7.3_

- [x] 7. 错误处理和安全入口点
- [x] 7.1 实现认证入口点
  - 创建 `DelegatingAuthenticationEntryPoint` 根据请求类型返回不同响应
  - 实现 API 请求的 JSON 错误响应
  - 实现表单请求的登录页面重定向
  - 编写入口点的单元测试
  - _需求: 4.2, 4.4_

- [x] 7.2 实现访问拒绝处理器
  - 创建 `DelegatingAccessDeniedHandler` 处理访问拒绝情况
  - 根据请求类型返回 JSON 错误或重定向
  - 编写访问拒绝处理的测试
  - _需求: 3.1_

- [x] 8. 令牌刷新端点实现
- [x] 8.1 创建令牌刷新控制器
  - 实现 `/api/token/refresh` 端点处理刷新请求
  - 支持 JSON 和表单数据两种请求格式
  - 集成 RefreshTokenService 进行令牌刷新
  - 返回新的访问令牌和刷新令牌
  - 编写控制器的单元测试和集成测试
  - _需求: 3.2, 3.3, 3.5_

- [x] 8.2 配置刷新端点安全规则
  - 在安全配置中为刷新端点禁用 CSRF 保护
  - 配置端点的访问权限（允许匿名访问但验证刷新令牌）
  - 编写安全配置的测试
  - _需求: 3.4_

- [x] 9. 记住我功能实现
- [x] 9.1 配置记住我服务
  - 配置 `RememberMeServices`（TokenBased 或 PersistentTokenBased）
  - 集成记住我功能与表单登录
  - 配置记住我 Cookie 的安全属性
  - 编写记住我功能的集成测试
  - _需求: 5.1, 5.2, 5.4_

- [x] 9.2 实现记住我认证集成
  - 确保记住我认证成功后建立用户会话
  - 集成记住我功能与有状态会话管理
  - 编写记住我与会话管理的集成测试
  - _需求: 5.3_

- [x] 10. 会话管理配置
- [x] 10.1 配置有状态会话管理
  - 配置 Spring Session 与 Redis 集成
  - 实现在线用户管理功能（统计、查看、踢出）
  - 配置会话固定攻击防护
  - 编写会话管理的集成测试
  - _需求: 6.1, 6.2_

- [x] 10.2 实现混合会话策略
  - 配置同时支持有状态和无状态模式
  - 根据请求上下文选择会话策略
  - 编写混合会话策略的测试
  - _需求: 6.4_

- [x] 11. 安全过滤器链配置
- [x] 11.1 配置主要安全过滤器链
  - 创建 `SecurityConfig` 配置类定义 `SecurityFilterChain` Bean
  - 配置不同路径的安全规则和 CSRF 保护策略
  - 集成所有自定义过滤器、转换器、提供者和处理器
  - 配置请求匹配器精确控制安全规则应用
  - _需求: 8.3, 10.4_

- [x] 11.2 配置认证管理器
  - 配置 `AuthenticationManager` 和多个 `AuthenticationProvider`
  - 设置提供者的优先级和支持的认证类型
  - 编写认证管理器配置的测试
  - _需求: 7.1, 7.4_

- [x] 12. JWT 编码解码器配置
- [x] 12.1 配置 JWT 编码器和解码器
  - 配置 `JwtEncoder` 和 `JwtDecoder` Bean
  - 设置 JWT 签名密钥和算法
  - 配置 JWT 声明和过期时间
  - 编写 JWT 编码解码的单元测试
  - _需求: 7.2, 2.2, 2.5_

- [x] 12.2 实现 JWT 令牌生成逻辑
  - 在 ApiAuthenticationSuccessHandler 中集成 JWT 生成
  - 实现 JWT 声明的自定义设置
  - 编写 JWT 令牌生成的集成测试
  - _需求: 2.2_

- [x] 13. 综合集成测试
- [x] 13.1 端到端认证流程测试
  - 编写表单登录完整流程的集成测试
  - 编写 API 登录完整流程的集成测试
  - 测试令牌认证和刷新的完整流程
  - 验证不同认证方式的响应格式
  - _需求: 1.1, 1.2, 2.1, 3.2_

- [x] 13.2 安全配置验证测试
  - 测试 CSRF 保护的选择性应用
  - 测试会话管理和记住我功能
  - 测试错误处理和安全入口点
  - 验证令牌过期和刷新机制
  - _需求: 3.1, 5.1, 6.1_

- [x] 13.3 性能和并发测试
  - 编写令牌验证性能测试
  - 测试高并发登录和令牌刷新场景
  - 验证内存令牌服务的并发安全性
  - _需求: 2.6, 3.2_

- [ ] 14. 文档和配置示例
- [ ] 14.1 创建配置文档
  - 编写 application.yml 配置示例
  - 创建 Redis 和数据库配置指南
  - 编写部署和安全配置最佳实践文档
  - _需求: 8.1_

- [ ] 14.2 创建使用示例
  - 编写客户端集成示例代码
  - 创建 API 调用示例和响应格式说明
  - 编写故障排除和常见问题解答
  - _需求: 4.3, 4.4_