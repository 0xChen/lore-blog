package com.developerchen.core.service.impl;

import com.developerchen.core.domain.entity.Log;
import com.developerchen.core.repository.LogMapper;
import com.developerchen.core.service.ILogService;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class LogServiceImpl extends BaseServiceImpl<LogMapper, Log> implements ILogService {

    public LogServiceImpl() {
    }


    @Override
    public void saveLog(Log log) {
        baseMapper.insert(log);
    }

    @Async(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    @Override
    public void asyncSaveLog(Log log) {
        baseMapper.insert(log);
    }

}
