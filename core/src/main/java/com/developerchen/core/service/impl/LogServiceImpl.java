package com.developerchen.core.service.impl;

import com.developerchen.core.domain.entity.Log;
import com.developerchen.core.repository.LogMapper;
import com.developerchen.core.service.ILogService;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveLog(Log log) {
        baseMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Async(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public void asyncSaveLog(Log log) {
        baseMapper.insert(log);
    }

}
