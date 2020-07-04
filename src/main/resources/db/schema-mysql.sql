--
-- mysql init schema
--

CREATE DATABASE IF NOT EXISTS lore_blog DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE lore_blog;

-- ----------------------------
-- Table structure for `sys_user`
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) unsigned NOT NULL,
  `nickname` varchar(25) NOT NULL COMMENT '昵称',
  `username` varchar(25) NOT NULL COMMENT '登陆用户名',
  `password` varchar(255) NOT NULL COMMENT '登陆密码',
  `email` varchar(100) NOT NULL COMMENT '电子邮箱',
  `status` varchar(2) NOT NULL COMMENT '状态',
  `role` varchar(2) NOT NULL COMMENT '角色',
  `description` varchar(255) DEFAULT NULL COMMENT '简介',
  `last_login` datetime(3) COMMENT '最近一次登陆时间',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table structure for `sys_option`
-- ----------------------------
DROP TABLE IF EXISTS `sys_option`;
CREATE TABLE `sys_option` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(50) NOT NULL COMMENT '设置项名称, 对应页面input的name属性',
  `label` varchar(100) NOT NULL COMMENT '设置项显示名称, 可以作为input的标题',
  `value` varchar(500) COMMENT '设置项值, input的value属性',
  `description` varchar(200) DEFAULT NULL COMMENT '描述, 可以作为input的placeholder',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设置表';

-- ----------------------------
--  Table structure for `sys_menu`
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
  `pid` bigint(20) DEFAULT NULL COMMENT '父级菜单ID',
  `status` varchar(2) NOT NULL COMMENT '菜单状态',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `url` varchar(255) DEFAULT NULL COMMENT '连接地址',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `level` int(11) DEFAULT NULL COMMENT '层级',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- Table structure for `sys_log`
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint(20) unsigned NOT NULL COMMENT '日志主键',
  `type` varchar(20) NOT NULL COMMENT '日志类型',
  `description` varchar(255) DEFAULT NULL COMMENT '日志描述',
  `ip` varchar(32) DEFAULT NULL COMMENT '操作人IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户标识',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求URI',
  `request_query` varchar(1000) DEFAULT NULL COMMENT '请求URL后的查询参数',
  `request_method` varchar(255) DEFAULT NULL COMMENT '请求方法(get, post, ...)',
  `method` varchar(500) DEFAULT NULL COMMENT '调用方法',
  `arguments` text DEFAULT NULL  COMMENT '调用方法的入参',
  `elapsed_time` int(8) DEFAULT NULL COMMENT '调用方法耗时ms',
  `exception` text DEFAULT NULL COMMENT '异常信息',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';

-- ----------------------------
-- Table structure for `blog_post`
-- ----------------------------
DROP TABLE IF EXISTS `blog_post`;
CREATE TABLE `blog_post` (
  `id` bigint(20) unsigned NOT NULL,
  `title` varchar(200) NOT NULL COMMENT '标题',
  `slug` varchar(300) COMMENT '文章缩略名, 用于自定义访问路径',
  `author_id` bigint(20) NOT NULL COMMENT '作者',
  `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图',
  `content` longtext COMMENT '文章内容',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类',
  `type` varchar(10) NOT NULL COMMENT 'post类型文章, 页面等',
  `content_type` varchar(10) NOT NULL COMMENT '内容类型html, markdown等',
  `status` varchar(2) NOT NULL COMMENT '文章状态',
  `comment_status` varchar(2) NOT NULL COMMENT '评论状态',
  `ping_status` varchar(2) NOT NULL,
  `comment_count` int(10) unsigned DEFAULT 0 COMMENT '评论数量',
  `read_count` int(10) unsigned DEFAULT 0 COMMENT '阅读次数',
  `pubdate` datetime(3) DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章与页面表';

-- ----------------------------
-- Table structure for `blog_comment`
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment` (
  `id` bigint(20) unsigned NOT NULL,
  `owner_id` bigint(20) COMMENT '评论所属的主体',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级评论',
  `author_id` bigint(20) DEFAULT NULL COMMENT '评论者ID',
  `author_name` varchar(25) NOT NULL DEFAULT '' COMMENT '评论者昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `url` varchar(200) DEFAULT NULL COMMENT '评论者网址',
  `ip` varchar(100) DEFAULT NULL COMMENT '评论者IP',
  `agent` varchar(255) DEFAULT NULL COMMENT '评论者客户端',
  `content` text NOT NULL COMMENT '评论内容',
  `status` varchar(2) NOT NULL  COMMENT '评论状态',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- Table structure for `blog_link`
-- ----------------------------
DROP TABLE IF EXISTS `blog_link`;
CREATE TABLE `blog_link` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(100) NOT NULL COMMENT '网站名称',
  `url` varchar(255) DEFAULT NULL COMMENT '网站链接',
  `sort` int(11) unsigned NOT NULL COMMENT '排序',
  `description` varchar(255) DEFAULT NULL COMMENT '网站描述',
  `visible` varchar(2) NOT NULL DEFAULT '1' COMMENT '是否显示',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链接表';

-- ----------------------------
-- Table structure for `blog_category`
-- ----------------------------
DROP TABLE IF EXISTS `blog_category`;
CREATE TABLE `blog_category` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(25) NOT NULL COMMENT '分类名称',
  `left_value` int(11) NOT NULL COMMENT '左值',
  `right_value` int(11) NOT NULL COMMENT '右值',
  `visible` varchar(2) NOT NULL DEFAULT '1' COMMENT '是否可见[0]不可见, [1]可见',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ----------------------------
--  Table structure for `sys_attachment`
-- ----------------------------
DROP TABLE IF EXISTS `sys_attachment`;
CREATE TABLE `sys_attachment` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(50) NOT NULL COMMENT '附件在磁盘中的文件名, 附件上传后会生成一个唯一的文件名并已这个名字保存到磁盘中',
  `original_name` varchar(100) NOT NULL DEFAULT '' COMMENT '原始文件名',
  `type` varchar(50) COMMENT '附件类型',
  `size` bigint(20) COMMENT '附件大小',
  `key` varchar(100) DEFAULT NULL COMMENT '文件的唯一标识',
  `description` varchar(255) DEFAULT NULL COMMENT '附件描述',
  `height` smallint(5) unsigned COMMENT '如果是图片类型存放图片的高度',
  `width` smallint(5) unsigned COMMENT '如果是图片类型存放图片的宽度',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '修改人',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表 保存文件、图片等';
