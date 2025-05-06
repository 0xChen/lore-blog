package com.developerchen.core.logging.annotation;

import com.developerchen.core.common.constant.Const;

import java.lang.annotation.*;

/**
 * 记录操作日志
 *
 * @author syc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    String INFO = Const.LOG_INFO;// 普通信息日志
    String WARN = Const.LOG_WARN;// 警告日志
    String ERROR = Const.LOG_ERROR;// 异常日志

    /**
     * 日志类型
     */
    String type() default INFO;


    /**
     * 当前正在进行的操作的描述
     */
    String desc() default "";
}
