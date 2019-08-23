package com.developerchen.core.initializer;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import com.developerchen.core.security.JwtTokenUtil;
import com.developerchen.core.service.IOptionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 执行一些系统初始化工作
 *
 * @author syc
 */
@Component
public class AppInitializer {

    private IOptionService optionService;


    public AppInitializer(IOptionService optionService) {
        this.optionService = optionService;
    }

    /**
     * 应用启动时一些初始化工作
     *
     * @throws IOException 创建保存上传文件的目录失败时
     */
    @PostConstruct
    private void initAppConfig() throws IOException {
        // 读取数据库 sys_option 表中所有配置项, 添加到SystemConfig.OPTION中
        Map<String, String> option = optionService.getAllOption();
        AppConfig.addOptions(option);
        String hostname = option.get("hostname");
        if (StringUtils.isNotBlank(hostname)) {
            // 数据库中设置的域名优先级高于配置文件中指定的
            AppConfig.hostname = hostname;
        }

        // 读取设定, 创建保存上传文件的目录, 如果指定目录已存在则跳过
        Path path = Paths.get(AppConfig.fileLocation);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IOException("用于保存上传文件的文件夹初始化失败，请检查配置文件中的[my-app:file-location]配置", e);
            }
        }

        // 初始化jwt
        JwtTokenUtil.initSecretKey();

        // 初始化静态常量Map
        try {
            Const.HOSTNAME = AppConfig.hostname;
            Const.init();
            BlogConst.init();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("初始化静态常量失败");
        }

    }

}
