# 多格式认证系统使用说明

## 概述

本认证系统基于Spring Security 6.4.*，支持以下功能：

- 支持JWT格式和自定义格式的令牌
- 自动识别令牌格式或通过header标记识别
- 支持表单提交和POST JSON数据两种登录方式
- 支持从请求头、URL参数和Cookie中获取令牌

## 配置说明

### 1. 启用多格式认证系统

在应用配置类上导入`MultiFormatSecurityConfig`：

```java
@Import(MultiFormatSecurityConfig.class)
@SpringBootApplication
public class Application {
    // ...
}
```

### 2. 配置令牌参数

在`application.properties`或`application.yml`中配置：

```properties
# JWT令牌配置
jwt.expire-time=86400000
jwt.secret-key=YourJwtSecretKey
jwt.secret-key-path=/path/to/jwt/secret/file

# 自定义令牌配置
custom.token.expire-time=86400000
custom.token.secret-key=YourCustomTokenSecretKey
```

## 使用说明

### 1. 登录方式

#### 表单登录

```html
<form action="/admin/login" method="post">
    <input type="text" name="username">
    <input type="password" name="password">
    <button type="submit">登录</button>
</form>
```

#### JSON登录

```javascript
fetch('/admin/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        username: 'admin',
        password: 'password'
    })
});
```

### 2. 令牌使用

#### JWT令牌

```javascript
// 在请求头中使用
fetch('/api/resource', {
    headers: {
        'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
    }
});

// 指定令牌类型
fetch('/api/resource', {
    headers: {
        'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        'X-Token-Type': 'jwt'
    }
});
```

#### 自定义令牌

```javascript
// 在请求头中使用
fetch('/api/resource', {
    headers: {
        'Authorization': 'Bearer username:timestamp:authorities:signature'
    }
});

// 指定令牌类型
fetch('/api/resource', {
    headers: {
        'Authorization': 'Bearer username:timestamp:authorities:signature',
        'X-Token-Type': 'custom'
    }
});
```

### 3. 后端生成令牌

#### JWT令牌

```java
// 通过User对象生成JWT令牌
String jwtToken = JwtTokenUtil.generateToken(user);

// 通过ApiUser对象生成JWT令牌
String jwtToken = JwtTokenUtil.generateToken(apiUser);
```

#### 自定义令牌

```java
// 通过User对象生成自定义令牌
String customToken = CustomTokenUtil.generateToken(user);

// 通过ApiUser对象生成自定义令牌
String customToken = CustomTokenUtil.generateToken(apiUser);
```

## 安全建议

1. 在生产环境中，建议使用足够长且复杂的密钥
2. 定期轮换密钥以提高安全性
3. 根据业务需求调整令牌过期时间
4. 考虑使用HTTPS以保护令牌传输安全