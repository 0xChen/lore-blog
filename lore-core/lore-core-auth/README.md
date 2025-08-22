# lore-core-auth

Spring Security 认证授权模块

## 项目结构

```
src/main/java/com/developerchen/core/auth/
├── config/                     # 配置类
│   ├── SecurityProperties.java        # 安全配置属性
│   └── SecurityAutoConfiguration.java # 自动配置类
├── converter/                  # 认证转换器
├── provider/                   # 认证提供者
├── handler/                    # 认证处理器
├── filter/                     # 认证过滤器
├── service/                    # 服务接口和实现
└── user/                       # 用户相关类

src/main/resources/
├── META-INF/
│   ├── spring.factories                           # Spring Boot 自动配置
│   └── additional-spring-configuration-metadata.json # 配置元数据
└── application-auth-example.yml                   # 配置示例
```

## 核心配置

### SecurityProperties

类型安全的配置属性类，支持以下配置：

- `security.token.*` - 令牌相关配置
- `security.session.*` - 会话管理配置  
- `security.remember-me.*` - 记住我功能配置

### 配置示例

```yaml
security:
  token:
    access-token-expiration: PT30M
    refresh-token-expiration: P7D
    jwt-secret: ${JWT_SECRET:your-secret-key}
    jwt-issuer: lore-core
  session:
    stateful: true
    redis-namespace: "lore:session"
  remember-me:
    key: ${REMEMBER_ME_KEY:lore-remember-me}
    token-validity-seconds: P14D
```

## 依赖项

- Spring Boot 3.4.5
- Spring Security 6.5+
- Spring Security OAuth2 JOSE (JWT 支持)
- Spring Session Data Redis
- Spring Boot Starter Web

## 使用方式

该模块通过 Spring Boot 自动配置机制自动启用。只需在项目中添加依赖即可：

```gradle
implementation project(':lore-core:lore-core-auth')
```

## 任务进度

本模块正在按照 `.kiro/specs/spring-security-auth-module/tasks.md` 中的实现计划逐步开发。

当前已完成：
- ✅ 项目结构和核心配置设置
  - 创建了完整的包结构
  - 配置了 Gradle 依赖项和 Spring Boot 版本
  - 实现了 SecurityProperties 配置属性类
  - 设置了 Spring Boot 自动配置机制
  - 创建了配置元数据和示例文件
