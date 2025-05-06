package com.developerchen.core.logging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.core.logging.entity.Log;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;

public interface ILogService {

    /**
     * 保存日志
     */
    void saveLog(Log log);

    /**
     * 异步保存日志
     */
    @Async
    void asyncSaveLog(Log log);

    /**
     * 分页查询日志
     */
    Page<Log> getLogPage(int current, int size, String orderBy, String orderDirection,
                         LocalDateTime startTime, LocalDateTime endTime,
                         String logType, String httpMethod, String requestUri);

    /**
     * 按ID删除日志
     */
    void deleteLogById(Long id);

    /**
     * 清理过期日志
     */
    void purgeLogs(int retainDays);
}
