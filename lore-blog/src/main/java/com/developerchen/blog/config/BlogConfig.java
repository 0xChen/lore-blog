package com.developerchen.blog.config;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.system.config.SystemConfig;
import com.developerchen.core.common.constant.Const;
import org.springframework.context.annotation.Configuration;

/**
 * BLOG的自定义配置
 *
 * @author syc
 */

@Configuration
public class BlogConfig {
    public static String ALLOW_COMMENT_APPROVE = SystemConfig.getOption(
            BlogConst.OPTION_ALLOW_COMMENT_APPROVE, Const.NO);
}
