package com.developerchen.core.system.web.admin;

import com.developerchen.core.base.BaseController;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.system.config.SystemConfig;
import com.developerchen.core.system.service.IOptionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配置项管理
 *
 * @author syc
 */
@RestController
@RequestMapping("/admin/options")
public class OptionAdminController extends BaseController {

    private final IOptionService optionService;


    public OptionAdminController(IOptionService optionService) {
        this.optionService = optionService;
    }

    /**
     * 保存配置项, 有则更新, 无则新增
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, path = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<?> upsertOptions(@RequestBody Map<String, String> parameterMap) {
        optionService.upsertOptions(parameterMap);
        return R.ok();
    }

    /**
     * 获取所有配置项
     */
    @GetMapping("/list")
    public R<Map<String, String>> getOptions() {
        return R.ok(SystemConfig.OPTIONS);
    }

}
