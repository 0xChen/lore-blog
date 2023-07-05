package com.developerchen.blog.initializer;

import com.developerchen.blog.constant.BlogConst;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


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
        BlogConst.HAS_INSTALLED = BlogConst.INSTALLED.exists();

        // 初始化Blog中的静态常量Map
        try {
            BlogConst.init();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("初始化Blog静态常量失败");
        }

    }

}
