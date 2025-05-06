你是一个资深的java专家，请在开发中遵循如下规则：
- 严格遵循 **SOLID、DRY、KISS、YAGNI** 原则
- 遵循 **OWASP 安全最佳实践**（如输入验证、SQL注入防护）
- 采用 **分层架构设计**，确保职责分离
- 代码变更需通过 **单元测试覆盖**（测试覆盖率 ≥ 80%）

---

## 二、技术栈规范
### 技术栈要求
- **框架**：Spring Boot 3.4.x + Spring Framework 6.x + Java 23
- **依赖**：
    - 核心：Spring Boot, Spring Framework, Mybatis-plus, Lombok
    - 数据库：MySQL Driver
    - 其他：Spring Security (权限控制)

---

## 三、应用逻辑设计规范
### 1. 分层架构原则
| 层级          | 职责                                                                 | 约束条件                                                |
|---------------|----------------------------------------------------------------------|-----------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口                                 | - 禁止直接操作数据库<br>- 必须通过 Service 层调用                   |
| **Service**    | 业务逻辑实现，事务管理，数据校验                                   | - 必须通过 Repository 访问数据库<br>- 返回实体类                  |
| **Repository** | 数据持久化操作，定义数据库查询逻辑                                 | - 必须继承 com.developerchen.core.base 包下的 `BaseMapper` |
| **Entity**     | 数据库表结构映射对象                                               | -                |

---

## 四、核心代码规范
### 1. 实体类（Entity）规范
```java
package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Validated
@TableName("sys_user")
public class User extends BaseEntity {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * 昵称
    */
   @Length(max = 25, message = "昵称长度不能超过 25 个字符")
   private String nickname;
   /**
    * 登陆用户名
    */
   @NotNull(message = "登陆用户名不能为空")
   @Length(min = 3, max = 25, message = "登陆用户名长度必须在 3-25 个字符之间")
   private String username;
   /**
    * 登陆密码
    */
   @NotNull(message = "登陆用户名不能为空")
   @Length(max = 255, message = "密码长度不能超过 255 个字符")
   private String password;
   /**
    * 电子邮箱
    */
   @Email(message = "电子邮箱格式不正确")
   private String email;
}
```

### 2. 数据访问层（Repository）规范
```java
package com.developerchen.core.system.repository;

import com.developerchen.core.base.BaseMapper;
import com.developerchen.core.domain.entity.User;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface UserMapper extends BaseMapper<User> {

   @Delete("DELETE FROM `sys_user` WHERE `id` <> 1")
   void purge();
}
```

### 3. 服务层（Service）规范
```java
package com.developerchen.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.developerchen.core.base.BaseServiceImpl;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.repository.UserMapper;
import com.developerchen.core.system.service.IUserService;
import com.developerchen.core.system.util.SecurityUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
@Primary
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements IUserService {


   @Override
   @Transactional(rollbackFor = {Exception.class, Error.class})
   public void upsert(User user) {
      String password = user.getPassword();
      if (password != null) {
         // 加密
         user.setPassword(SecurityUtils.encodeUserPassword(password));
      }
      super.saveOrUpdate(user);
   }
   
   @Override
   public User getUserById(Serializable id) {
      return baseMapper.selectById(id);
   }

   @Override
   @Transactional(rollbackFor = {Exception.class, Error.class})
   public void deleteUserById(Serializable id) {
      Validate.notNull(id, "用户ID不能为空, 删除失败!");
      baseMapper.deleteById(id);
   }

   @Override
   @Transactional(rollbackFor = {Exception.class, Error.class})
   public void purge() {
      baseMapper.purge();
   }
}

```

### 4. 控制器（RestController）规范
```java
package com.developerchen.core.system.web.admin;

import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户管理 前端控制器
 * </p>
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
public class UserAdminController extends BaseController {
   private final IUserService userService;


   /**
    * 管理权限的用户新增或更新其他用户信息
    *
    * @param user 新用户信息
    * @return 用户信息
    */
   @PostMapping("/upsert")
   public R<User> upsert(User user) {
      userService.upsert(user);
      return R.ok(user);
   }

   /**
    * 通过用户ID获取用户信息
    *
    * @param userId 用户ID
    * @return 用户信息
    */
   @GetMapping("/{userId}/detail")
   public R<User> getUserById(@PathVariable("userId") Long userId) {
      User user = userService.getUserById(userId);
      if (user != null) {
         user.setPassword(null);
         return R.ok(user);
      } else {
         return R.fail("没有此用户");
      }
   }

   /**
    * 通过用户ID删除用户
    *
    * @param userId 用户ID
    */
   @PostMapping("/{userId}/delete")
   public R<String> deleteUserById(@PathVariable("userId") Long userId) {
      userService.deleteUserById(userId);
      return R.ok();
   }
}

```

---

## 五、数据传输对象（DTO）规范
```java
// 使用 record 或 @Data 注解
public record UserDTO(
    @NotBlank String username,
    @Email String email
) {
    public static UserDTO fromEntity(User entity) {
        return new UserDTO(entity.getUsername(), entity.getEmail());
    }
}
```

---

## 六、全局异常处理规范
### 1. 统一响应类（R<T>）
```java
package com.developerchen.core.common.domain;

/**
 * 返回对象
 *
 * 自定义Http状态码从600开始
 * 600表示有需要alert的错误消息
 * 611表示有表示博客没有执行过安装初始化步骤, 需要执行安装程序
 * @param <T>
 * @author syc
 */
public class R<T> {

   /**
    * 响应数据
    */
   private T data;

   /**
    * 扩展数据
    */
   private Map<String, Object> extData = new HashMap<>(16);

   /**
    * 请求是否成功
    */
   private boolean success;

   /**
    * 信息
    */
   private String message;

   /**
    * Http状态码
    */
   private int status = -1;

   /**
    * 服务器响应时间
    */
   private long timestamp;

   // 省略一些构造方法和getter/setter方法
 

   public static <T> R<T> ok() {
      return new R<>(true, 200);
   }

   public static <T> R<T> ok(T payload) {
      return new R<>(true, payload, 200);
   }

   public static <T> R<T> ok(String message) {
      return new R<>(true, message, 200);
   }

   public static <T> R<T> ok(int status) {
      return new R<>(true, null, status);
   }

   public static <T> R<T> ok(T payload, int status) {
      return new R<>(true, payload, status);
   }


   public static <T> R<T> fail() {
      return new R<>(false);
   }

   public static <T> R<T> fail(String message) {
      return new R<>(false, message);
   }

   public static <T> R<T> fail(int status) {
      return new R<>(false, null, status);
   }

   public static <T> R<T> fail(int status, String message) {
      return new R<>(false, message, status);
   }

}

```

### 2. 全局异常处理器（GlobalExceptionHandler）

在 com.developerchen.core.base.support包下的 GlobalExceptionHandler.java文件


---

## 七、安全与性能规范
1. **输入校验**：
    - 使用 `@Valid` 注解 + JSR-303 校验注解（如 `@NotBlank`, `@Size`）
    - 禁止直接拼接 SQL 防止注入攻击
2. **事务管理**：
    - `@Transactional` 注解仅标注在 Service 方法上
    - 避免在循环中频繁提交事务
3. **性能优化**：
    - 避免在循环中执行数据库查询（批量操作优先）

---

## 八、代码风格规范
1. **命名规范**：
    - 类名：`UpperCamelCase`（如 `UserServiceImpl`）
    - 方法/变量名：`lowerCamelCase`（如 `saveUser`）
    - 常量：`UPPER_SNAKE_CASE`（如 `MAX_LOGIN_ATTEMPTS`）
2. **注释规范**：
    - 方法必须添加注释且方法级注释使用 Javadoc 格式
    - 计划待完成的任务需要添加 `// TODO` 标记
    - 存在潜在缺陷的逻辑需要添加 `// FIXME` 标记
3. **代码格式化**：
    - 使用 IntelliJ IDEA 默认的 Spring Boot 风格
    - 禁止手动修改代码缩进（依赖 IDE 自动格式化）

---

## 九、部署规范
1. **部署规范**：
    - 生产环境需禁用 `@EnableAutoConfiguration` 的默认配置
    - 敏感信息通过 `application.yml` 外部化配置
    - 使用 `Spring Profiles` 管理环境差异（如 `dev`, `prod`）

---

## 十、扩展性设计规范
1. **接口优先**：
    - 服务层接口（`IUserService`）与实现（`UserServiceImpl`）分离
2. **扩展点预留**：
    - 关键业务逻辑需提供 `Strategy` 或 `Template` 模式支持扩展
3. **日志规范**：
    - 使用 `SLF4J` 记录日志（禁止直接使用 `System.out.println`）
    - 核心操作需记录 `INFO` 级别日志，异常记录 `ERROR` 级别
```
