package com.developerchen.core.logging.web.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.logging.entity.Log;
import com.developerchen.core.logging.service.ILogService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/logs")
@AllArgsConstructor
public class LogAdminController {

    private final ILogService logService;

    @PostMapping("/save")
    public R<Log> saveLog(@RequestBody Log log) {
        logService.saveLog(log);
        return R.ok(log);
    }

    @PostMapping("/async-save")
    public R<String> asyncSaveLog(@RequestBody Log log) {
        logService.asyncSaveLog(log);
        return R.ok("日志已异步保存");
    }

    @GetMapping("/page")
    public R<Page<Log>> getLogPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "DESC") String orderDirection,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String requestUri) {

        Page<Log> page = logService.getLogPage(
                current, size,
                StringUtils.defaultIfBlank(orderBy, "create_time"),
                orderDirection,
                startTime, endTime,
                logType, httpMethod, requestUri
        );
        return R.ok(page);
    }

    @PostMapping("/{logId}/delete")
    public R<String> deleteLogById(@PathVariable Long logId) {
        logService.deleteLogById(logId);
        return R.ok("日志删除成功");
    }

    @PostMapping("/purge")
    public R<String> purgeLogs(@RequestParam(defaultValue = "30") int retainDays) {
        logService.purgeLogs(retainDays);
        return R.ok("日志清理完成，保留最近" + retainDays + "天数据");
    }
}