package com.developerchen.core.logging.util;


import com.developerchen.core.base.support.SpringEventPublisher;
import com.developerchen.core.common.constant.Const;
import com.developerchen.core.logging.entity.Log;
import com.developerchen.core.base.event.EntityCreatingEvent;
import com.developerchen.core.common.util.RequestUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * 日志工具类
 *
 * @author syc
 */
@Slf4j
public class LogUtils {


    /**
     * 记录一条日志。
     *
     * @param msg 要记录的消息字符串
     */
    public static void info(String msg) {
        createAndPublishLog(Const.LOG_INFO, msg, null, null);
    }

    /**
     * 记录一条日志。
     *
     * @param msg    要记录的消息字符串
     * @param logger 要记录日志的类中的 Logger 对象
     */
    public static void info(Logger logger, String msg) {
        createAndPublishLog(Const.LOG_INFO, msg, null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     */
    public static void info(String format, Object arg) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arg), null, null);
    }


    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     * @param logger 要记录日志的类中的 Logger 对象
     */
    public static void info(Logger logger, String format, Object arg) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arg), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void info(String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arg1, arg2), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     * @param logger 要记录日志的类中的 Logger 对象
     */
    public static void info(Logger logger, String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arg1, arg2), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     */
    public static void info(String format, Object... arguments) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arguments), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     * @param logger    要记录日志的类中的 Logger 对象
     */
    public static void info(Logger logger, String format, Object... arguments) {
        createAndPublishLog(Const.LOG_INFO, formatMessage(format, arguments), null, logger);
    }

    /**
     * 记录一条带有异常（throwable）的日志。
     *
     * @param msg 伴随异常的消息
     * @param t   要记录的异常（throwable）
     */
    public static void info(String msg, Throwable t) {
        createAndPublishLog(Const.LOG_INFO, msg, t, null);
    }

    /**
     * 记录一条带有异常（throwable）的日志。
     *
     * @param msg    伴随异常的消息
     * @param t      要记录的异常（throwable）
     * @param logger 要记录日志的类中的 Logger 对象
     */
    public static void info(Logger logger, String msg, Throwable t) {
        createAndPublishLog(Const.LOG_INFO, msg, t, logger);
    }


    // =========================== warn

    /**
     * 记录一条警告日志。
     *
     * @param msg 要记录的消息字符串
     */
    public static void warn(String msg) {
        createAndPublishLog(Const.LOG_WARN, msg, null, null);
    }

    /**
     * 记录一条警告日志。
     *
     * @param msg    要记录的消息字符串
     * @param logger 要记录警告日志的类中的 Logger 对象
     */
    public static void warn(Logger logger, String msg) {
        createAndPublishLog(Const.LOG_WARN, msg, null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     */
    public static void warn(String format, Object arg) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arg), null, null);
    }


    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     * @param logger 要记录警告日志的类中的 Logger 对象
     */
    public static void warn(Logger logger, String format, Object arg) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arg), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void warn(String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arg1, arg2), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     * @param logger 要记录警告日志的类中的 Logger 对象
     */
    public static void warn(Logger logger, String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arg1, arg2), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     */
    public static void warn(String format, Object... arguments) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arguments), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条警告日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     * @param logger    要记录警告日志的类中的 Logger 对象
     */
    public static void warn(Logger logger, String format, Object... arguments) {
        createAndPublishLog(Const.LOG_WARN, formatMessage(format, arguments), null, logger);
    }

    /**
     * 记录一条带有异常（throwable）的警告日志。
     *
     * @param msg 伴随异常的消息
     * @param t   要记录的异常（throwable）
     */
    public static void warn(String msg, Throwable t) {
        createAndPublishLog(Const.LOG_WARN, msg, t, null);
    }

    /**
     * 记录一条带有异常（throwable）的警告日志。
     *
     * @param msg    伴随异常的消息
     * @param t      要记录的异常（throwable）
     * @param logger 要记录警告日志的类中的 Logger 对象
     */
    public static void warn(Logger logger, String msg, Throwable t) {
        createAndPublishLog(Const.LOG_WARN, msg, t, logger);
    }

    // =========================== error

    /**
     * 记录一条错误日志。
     *
     * @param msg 要记录的消息字符串
     */
    public static void error(String msg) {
        createAndPublishLog(Const.LOG_ERROR, msg, null, null);
    }

    /**
     * 记录一条错误日志。
     *
     * @param msg    要记录的消息字符串
     * @param logger 要记录错误日志的类中的 Logger 对象
     */
    public static void error(Logger logger, String msg) {
        createAndPublishLog(Const.LOG_ERROR, msg, null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     */
    public static void error(String format, Object arg) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arg), null, null);
    }


    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format 格式字符串
     * @param arg    参数
     * @param logger 要记录错误日志的类中的 Logger 对象
     */
    public static void error(Logger logger, String format, Object arg) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arg), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     */
    public static void error(String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arg1, arg2), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format 格式字符串
     * @param arg1   第一个参数
     * @param arg2   第二个参数
     * @param logger 要记录错误日志的类中的 Logger 对象
     */
    public static void error(Logger logger, String format, Object arg1, Object arg2) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arg1, arg2), null, logger);
    }

    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     */
    public static void error(String format, Object... arguments) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arguments), null, null);
    }

    /**
     * 根据指定的格式和参数记录一条错误日志。
     *
     * @param format    格式字符串
     * @param arguments 三个或更多参数的列表
     * @param logger    要记录错误日志的类中的 Logger 对象
     */
    public static void error(Logger logger, String format, Object... arguments) {
        createAndPublishLog(Const.LOG_ERROR, formatMessage(format, arguments), null, logger);
    }

    /**
     * 记录一条带有异常（throwable）的错误日志。
     *
     * @param msg 伴随异常的消息
     * @param t   要记录的异常（throwable）
     */
    public static void error(String msg, Throwable t) {
        createAndPublishLog(Const.LOG_ERROR, msg, t, null);
    }

    /**
     * 记录一条带有异常（throwable）的错误日志。
     *
     * @param msg    伴随异常的消息
     * @param t      要记录的异常（throwable）
     * @param logger 要记录错误日志的类中的 Logger 对象
     */
    public static void error(Logger logger, String msg, Throwable t) {
        createAndPublishLog(Const.LOG_ERROR, msg, t, logger);
    }


    /**
     * 核心方法：创建 Log 实体并发布日志创建事件。
     * 如果传入 logger 同时使用 SLF4J 记录日志到控制台/文件。
     *
     * @param type        日志类型 (例如, Const.LOG_INFO, Const.LOG_WARN, Const.LOG_ERROR)。
     * @param description 格式化后的日志描述信息。
     * @param throwable   可选的、与日志关联的异常对象。
     */
    private static void createAndPublishLog(String type, String description, Throwable throwable, Logger logger) {
        String callerMethodFullName = getCallerMethodFullName();

        // 1. 创建 Log 实体
        Log logEntity = new Log();
        logEntity.setType(type);
        logEntity.setMethod(callerMethodFullName);
        logEntity.setDescription(description);
        logEntity.setRequestUri(RequestUtils.getRequestURI());
        logEntity.setRequestQuery(RequestUtils.getRequestQueryString());
        logEntity.setRequestMethod(RequestUtils.getRequestMethod());
        logEntity.setIp(RequestUtils.getRemoteIp());
        logEntity.setUserAgent(RequestUtils.getUserAgent());

        // 2. 如果有异常信息，处理异常构造堆栈信息
        if (throwable != null) {
            logEntity.setStackTrace(ExceptionUtils.getStackTrace(throwable));
        }

        // 3. 使用 SLF4J 记录日志，方便开发者立即看到控制台输出
        if (logger != null) {
            if (Const.LOG_WARN.equals(type)) {
                logger.warn(description, throwable);
            } else if (Const.LOG_ERROR.equals(type)) {
                logger.error(description, throwable);
            } else { // 默认为 INFO
                logger.info(description);
            }
        }

        // 4. 发布实体创建事件
        if (SpringEventPublisher.publishEvent(new EntityCreatingEvent<>(logEntity))) {
            // 如果不等于 null 则前面已经记录了日志，无需重复记录
            if (logger == null) {
                // 如果事件发布失败，直接使用 SLF4J 记录日志
                if (Const.LOG_WARN.equals(type)) {
                    log.warn(description, throwable);
                } else if (Const.LOG_ERROR.equals(type)) {
                    log.error(description, throwable);
                } else { // 默认为 INFO
                    log.info(description);
                }
            }
        }
    }

    /**
     * 获取调用者的方法全名
     *
     * @return 方法全名，如果获取失败将返回 null
     */
    private static String getCallerMethodFullName() {
        String callerMethodFullName = null;
        // 1. 获取当前线程的堆栈跟踪
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 2. 确定调用者的堆栈元素索引
        //    - stackTrace[0] 是 Thread.getStackTrace() 方法本身
        //    - stackTrace[1] 是 LogUtils.getCallerMethodFullName() (本方法)
        //    - stackTrace[2] 是 LogUtils.createAndPublishLog (私有辅助方法)
        //    - stackTrace[3] LogUtils 的公共日志方法 (如 info, warn, error)
        //    - stackTrace[4] 真正调用 LogUtils 公共方法的外部方法
        int callerIndex = 4;

        // 健壮性检查：确保堆栈足够深
        if (stackTrace.length > callerIndex && stackTrace[callerIndex] != null) {
            StackTraceElement callerElement = stackTrace[callerIndex];

            // 3. 从堆栈元素中提取信息
            String callerClassName = callerElement.getClassName(); // 获取完整类名 (包括包名)
            String callerMethodName = callerElement.getMethodName(); // 获取方法名

            // 4. 组合成完整方法路径
            callerMethodFullName = callerClassName + "." + callerMethodName;
        }
        return callerMethodFullName;
    }


    /**
     * 格式化日志描述信息
     *
     * @param format 格式字符串
     * @param args   三个或更多参数的列表
     * @return 格式化后的日志描述信息
     */
    private static String formatMessage(String format, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        return ft.getMessage();
    }
}
