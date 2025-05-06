package com.developerchen.core.logging.listener;

import com.developerchen.core.base.event.EntityCreatingEvent;
import com.developerchen.core.logging.entity.Log;
import com.developerchen.core.logging.service.ILogService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听各类事件, 执行相应功能
 *
 * @author syc
 */
@Component
public class LogEventListener {

    private final ILogService logService;


    public LogEventListener(ILogService logService) {
        this.logService = logService;
    }

    /**
     * 监听Log创建事件
     *
     * @param logEvent logEvent
     */
    @EventListener
    public void log(EntityCreatingEvent<Log> logEvent) {
        logService.asyncSaveLog(logEvent.getSource());
    }
}
