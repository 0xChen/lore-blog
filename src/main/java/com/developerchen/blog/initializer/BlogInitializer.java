package com.developerchen.blog.initializer;

import com.developerchen.blog.constant.BlogConst;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Blog初始化工作
 *
 * @author syc
 */
@Component
public class BlogInitializer {

    public BlogInitializer() {
    }

    /**
     * 应用启动时一些初始化工作
     */
    @PostConstruct
    private void initialize() {
        
        // 初始化应用的安装状态
        if (BlogConst.INSTALLED.exists()) {
            BlogConst.HAS_INSTALLED = true;
        } else {
            BlogConst.HAS_INSTALLED = false;
        }

        // 初始化Blog中的静态常量Map
        try {
            BlogConst.init();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("初始化Blog静态常量失败");
        }

    }

}
