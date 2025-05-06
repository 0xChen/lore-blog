package com.developerchen.core.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.core.base.BaseServiceImpl;
import com.developerchen.core.logging.entity.Log;
import com.developerchen.core.logging.repository.LogMapper;
import com.developerchen.core.logging.service.ILogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
@Primary
public class LogServiceImpl extends BaseServiceImpl<LogMapper, Log> implements ILogService {

    public LogServiceImpl() {
    }


    @Override
    public void saveLog(Log log) {
        baseMapper.insert(log);
    }

    @Override
    @Async(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public void asyncSaveLog(Log log) {
        baseMapper.insert(log);
    }

    @Override
    public Page<Log> getLogPage(int current, int size, String orderBy, String orderDirection,
                                LocalDateTime startTime, LocalDateTime endTime,
                                String logType, String httpMethod, String requestUri) {
        Page<Log> page = new Page<>(current, size);
        QueryWrapper<Log> wrapper = new QueryWrapper<>();

        // 构建时间范围查询条件
        if (startTime != null && endTime != null) {
            wrapper.between("create_time", startTime, endTime);
        }

        // 构建其他查询条件
        Optional.ofNullable(logType).ifPresent(v -> wrapper.eq("log_type", v));
        Optional.ofNullable(httpMethod).ifPresent(v -> wrapper.eq("http_method", v));
        Optional.ofNullable(requestUri).ifPresent(v -> wrapper.like("request_uri", v));

        // 构建排序条件
        wrapper.orderBy(StringUtils.isNotBlank(orderBy), "ASC".equalsIgnoreCase(orderDirection), orderBy);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogById(Long id) {
        Validate.notNull(id, "日志ID不能为空");
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purgeLogs(int retainDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retainDays);
        baseMapper.delete(new QueryWrapper<Log>().lt("create_time", cutoffDate));
    }
}
