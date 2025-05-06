package com.developerchen.core.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.developerchen.core.base.event.EntityCreatingEvent;
import com.developerchen.core.common.constant.Const;
import com.developerchen.core.common.util.RequestUtils;
import com.developerchen.core.logging.entity.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
public class LogEventPublishAppender extends AppenderBase<ILoggingEvent> {

    private static ApplicationEventPublisher applicationEventPublisher;


    public LogEventPublishAppender() {
        super();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (applicationEventPublisher == null) {
            // applicationEventPublisher 初始化之前不发送 event
            return;
        }

        String message = event.getFormattedMessage();
        String level = event.getLevel().toString();
        long timeStamp = event.getTimeStamp();
        String loggerName = event.getLoggerName();
        Throwable throwable = (event.getThrowableProxy() instanceof ThrowableProxy tp) ? tp.getThrowable() : null;

        String type = Const.LOG_INFO;
        if (Const.LOG_WARN.equalsIgnoreCase(level)) {
            type = Const.LOG_WARN;
        } else if (Const.LOG_ERROR.equalsIgnoreCase(level)) {
            type = Const.LOG_ERROR;
        }

        // 1. 创建 Log 实体
        Log logEntity = new Log();
        logEntity.setType(type);
        logEntity.setMethod(loggerName);
        logEntity.setDescription(message);
        logEntity.setRequestUri(RequestUtils.getRequestURI());
        logEntity.setRequestQuery(RequestUtils.getRequestQueryString());
        logEntity.setRequestMethod(RequestUtils.getRequestMethod());
        logEntity.setIp(RequestUtils.getRemoteIp());
        logEntity.setUserAgent(RequestUtils.getUserAgent());
        logEntity.setCreateTime(new Date(timeStamp));

        // 2. 如果有异常信息，处理异常构造堆栈信息
        if (throwable != null) {
            logEntity.setStackTrace(ExceptionUtils.getStackTrace(throwable));
        }

        applicationEventPublisher.publishEvent(new EntityCreatingEvent<>(logEntity));
    }

    @Component
    static class InitLogEventPublishAppender implements ApplicationEventPublisherAware {
        @Override
        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            LogEventPublishAppender.applicationEventPublisher = applicationEventPublisher;
        }
    }
}
