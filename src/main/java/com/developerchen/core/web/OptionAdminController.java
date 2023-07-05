package com.developerchen.core.web;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.RestResponse;
import com.developerchen.core.service.IOptionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 配置项管理
 *
 * @author syc
 */
@Controller
@RequestMapping("/admin")
public class OptionAdminController extends BaseController {

    private final IOptionService optionService;


    public OptionAdminController(IOptionService optionService) {
        this.optionService = optionService;
    }

    /**
     * 保存配置项, 有则更新, 无则新增
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, path = "/options", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<?> saveOrUpdateOptions(@RequestBody Map<String, String> parameterMap) {
        optionService.saveOrUpdateOptions(parameterMap);
        return RestResponse.ok();
    }

    /**
     * 获取所有配置项
     */
    @ResponseBody
    @GetMapping("/options")
    public RestResponse<Map<String, String>> getOptions() {
        return RestResponse.ok(AppConfig.OPTIONS);
    }

}
