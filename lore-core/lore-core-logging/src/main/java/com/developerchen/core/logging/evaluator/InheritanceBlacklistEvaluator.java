package com.developerchen.core.logging.evaluator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 继承关系黑名单评估器（排除继承指定父类或实现指定接口的类）
 *
 * @author syc
 */
public class InheritanceBlacklistEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    // 配置参数：需要排除的父类或接口全限定名（多个用逗号分隔）
    @Getter
    @Setter
    private String deniedClasses;
    private final List<Class<?>> blacklistClasses = new ArrayList<>();

    @Override
    public void start() {
        if (deniedClasses != null && !deniedClasses.trim().isEmpty()) {
            String substitutedClasses;
            try {
                substitutedClasses = OptionHelper.substVars(deniedClasses, context);
            } catch (ScanException e) {
                substitutedClasses = "";
                addError("Failed to substitute variables in deniedClasses: " + deniedClasses, e);
            }

            if (substitutedClasses != null && !substitutedClasses.trim().isEmpty()) {
                String[] classNames = substitutedClasses.split(",");
                for (String className : classNames) {
                    String trimmedName = className.trim();
                    if (!trimmedName.isEmpty()) {
                        try {
                            Class<?> clazz = Class.forName(trimmedName, true, Thread.currentThread().getContextClassLoader());
                            blacklistClasses.add(clazz);
                        } catch (ClassNotFoundException e) {
                            addError("Class/Interface not found: " + trimmedName, e);
                        }
                    }
                }
            }
        }

        if (blacklistClasses.isEmpty()) {
            addWarn("No blacklist classes/interfaces configured for InheritanceBlacklistEvaluator [" + getName() + "]");
        }
        super.start();
    }

    @Override
    public boolean evaluate(ILoggingEvent event) {
        if (!isStarted() || blacklistClasses.isEmpty()) {
            return false; // 未启动或未配置有效类时默认不拦截
        }

        String loggerName = event.getLoggerName();
        if (loggerName == null) {
            return false;
        }

        try {
            Class<?> targetClass = Class.forName(loggerName, true,
                    Thread.currentThread().getContextClassLoader());

            // 黑名单逻辑：只要匹配任意一个继承关系就返回true（表示需要拦截）
            for (Class<?> clazz : blacklistClasses) {
                if (clazz.isAssignableFrom(targetClass)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException e) {
            // 忽略非类名的Logger
        }
        return false;
    }
}
