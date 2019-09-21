package com.developerchen.core.service;

import com.developerchen.core.domain.entity.Log;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author syc
 */
public interface ILogService extends IBaseService<Log> {

    @Transactional
    void saveLog(Log log);

    @Transactional
    void asyncSaveLog(Log log);
}
