package com.developerchen.core.annotation;

import com.developerchen.core.constant.Const;

import java.lang.annotation.*;

/**
 * 记录操作日志
 *
 * @author syc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogInfo {

    /**
     * 日志类型
     */
    String type() default Const.LOG_INFO;


    /**
     * 当前正在进行的操作的描述
     */
    String desc() default "";

}
