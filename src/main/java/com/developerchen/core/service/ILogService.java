package com.developerchen.core.service;

import com.developerchen.core.domain.entity.Log;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author syc
 */
public interface ILogService extends IBaseService<Log> {

    void saveLog(Log log);

    void asyncSaveLog(Log log);
}
