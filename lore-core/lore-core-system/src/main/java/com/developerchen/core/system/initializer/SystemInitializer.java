package com.developerchen.core.system.initializer;

import com.developerchen.core.common.constant.Const;
import com.developerchen.core.system.security.JwtTokenUtil;
import com.developerchen.core.system.config.SystemConfig;
import com.developerchen.core.system.service.IOptionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

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
public class SystemInitializer {

    public SystemInitializer() {
    }

    static class InitSystem implements ApplicationListener<ApplicationStartedEvent> {
        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            IOptionService optionService = event.getApplicationContext().getBean(IOptionService.class);
            initSystemConfig(optionService);
        }

        /**
         * 应用启动时一些初始化工作
         */
        private void initSystemConfig(IOptionService optionService) {
            // 读取数据库 sys_option 表中所有配置项, 添加到SystemConfig.OPTION中
            Map<String, String> option = optionService.getAllOption();
            SystemConfig.addOptions(option);
            String scheme = option.get(Const.OPTION_SCHEME);
            String hostname = option.get(Const.OPTION_HOSTNAME);
            if (StringUtils.isNotBlank(scheme)) {
                // 数据库中的设置优先级高于配置文件中指定的
                SystemConfig.scheme = scheme;
            }
            if (StringUtils.isNotBlank(hostname)) {
                // 数据库中设置的域名优先级高于配置文件中指定的
                SystemConfig.hostname = hostname;
            }

            // 读取设定, 创建保存上传文件的目录, 如果指定目录已存在则跳过
            Path path = Paths.get(SystemConfig.fileLocation);
            if (Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new RuntimeException("用于保存上传文件的文件夹初始化失败，请检查配置文件中的[lore.system:file-location]配置", e);
                }
            }

            // 初始化jwt
            JwtTokenUtil.initSecretKey();

            // 初始化静态常量Map
            try {
                Const.init();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("初始化静态常量失败");
            }

        }
    }

}
