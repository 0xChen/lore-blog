--
-- mysql init schema
--

-- ----------------------------
-- Table data for `blog_category`
-- ----------------------------
INSERT INTO `blog_category` (`id`, `name`, `left_value`, `right_value`)
VALUES (0, '默认分类', 1, 2);

-- ----------------------------
-- Table data for `sys_user`
-- ----------------------------
INSERT INTO `sys_user` (`id`, `nickname`, `username`, `password`, `email`, `status`, `role`, `description`)
VALUES (1, 'lore', 'blog', '', '', '2', '2', '由SQL初始化脚本添加的用户, 不能使用此用户登录系统. ');

-- ----------------------------
-- Table data for `blog_post`
-- ----------------------------
INSERT INTO `lore_blog`.`blog_post`(`id`, `title`, `slug`, `author_id`, `thumbnail`, `content`, `tags`, `category_id`,
                                    `type`, `content_type`, `status`, `comment_status`, `ping_status`, `comment_count`,
                                    `read_count`, `pubdate`, `create_time`, `update_time`)
VALUES (1164530409608876033, '演示文章', 'demo', 1, '',
        '# Lore-Blog 博客系统\n## 简介\n `Lore`是一个主要定位于像作者这样热衷折腾, 并在学习和使用[Spring](https://spring.io)技术栈的你而开发的单用户博客系统. \n \n > lore [lɔː(r)] n. 学问; 知识;   \n 希望这个博客系统能够让使用者: 学到学识, 记录学识, 传播学识.\n \n## 主要技术\n\n+ Spring Boot\n+ Spring Security\n+ Spring Framework\n+ Mybatis-plus\n+ Thymeleaf\n\n## 部分显示效果演示\n### 我是图片\n![演示图片](https://i.loli.net/2019/08/22/VobFDJ1CHZ8RQKE.jpg)\n### 我是代码块\n```java\npublic static void main(String[] args){\n    System.out.println(\"Hello  World\");\n}\n```\n### 我是表格\nFirst Header | Second Header\n------------ | -------------\nCell 1 | Cell 2\nCell 2 | Cell 4\n#### 我是超连接\n[点击访问作者博客网站](https://developerchen.com)  \n[点我查看Markdown教程](https://guides.github.com/features/mastering-markdown/)  \n\n**好啦, 祝您使用愉快.**\n\n',
        'demo', 1, 'post', 'markdown', '3', '1', '1', 0, 0, '2019-08-22 21:31:28.000', '2019-08-22 12:00:00.521',
        '2019-08-23 00:15:55.396');
INSERT INTO `blog_post`(`id`, `title`, `slug`, `author_id`, `thumbnail`, `content`, `tags`, `category_id`, `type`,
                        `content_type`, `status`, `comment_status`, `ping_status`, `comment_count`, `read_count`,
                        `pubdate`, `create_time`, `update_time`)
VALUES (1164720166989262849, '关于', 'about', 1, '',
        '# 如何自定义 \"关于\" 页面\n点击页面顶部 **\"导航栏\"** 中的 **\"关于\"** 其实就是访问链接 //hostname/about. 所以进入 **\"后台管理\"** 中的 **\"页面管理\"** 菜单项, 添加新页面然后将 \"页面名称\" 填写为 <u>about</u>  \n\n当然你也可以直接在 **\"页面管理\"** 中找到这个演示版的 \"关于页面\" 直接进行内容的修改.',
        NULL, 0, 'page', 'markdown', '3', '1', '0', 0, 4, '2019-08-23 10:05:05.000', '2019-08-23 10:05:01.921',
        '2019-08-23 11:17:58.747');
INSERT INTO `lore_blog`.`blog_post`(`id`, `title`, `slug`, `author_id`, `thumbnail`, `content`, `tags`, `category_id`,
                                    `type`, `content_type`, `status`, `comment_status`, `ping_status`, `comment_count`,
                                    `read_count`, `pubdate`, `create_time`, `update_time`)
VALUES (1164738605615214593, '友情链接', 'links', 1, '',
        '这是默认的 **\"友链\"** 页面, 这个页面的自定义方式和 **\"关于\"** 页面是一样.', NULL, 0, 'page', 'markdown', '3', '1', '0', 0, 0,
        '2019-08-23 11:19:37.030', '2019-08-23 11:18:18.032', '2019-08-23 11:19:37.031');
