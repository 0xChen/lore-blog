package com.developerchen.core.system.security;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;

/**
 * 标注重新授权客户端access_token
 *
 * @author syc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface RefreshToken {

    String value() default "";

}
