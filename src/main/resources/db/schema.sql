/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50534
Source Host           : localhost:3306
Source Database       : blog

Target Server Type    : MYSQL
Target Server Version : 50534
File Encoding         : 65001

Date: 2018-02-08 01:05:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `nickName` varchar(25) NOT NULL COMMENT '昵称',
  `realName` varchar(25) DEFAULT NULL,
  `password` varchar(128) NOT NULL,
  `email` varchar(100) NOT NULL,
  `status` tinyint(2) NOT NULL,
  `role` tinyint(2) unsigned NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_login` datetime COMMENT '最近一次登陆时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table structure for `options`
-- ----------------------------
DROP TABLE IF EXISTS `option`;
CREATE TABLE `option` (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `value` varchar(100) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设置表';

-- ----------------------------
--  Table structure for `menu`
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pid` bigint(20) unsigned DEFAULT NULL COMMENT '父级菜单ID',
  `status` tinyint(2) NOT NULL,
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `url` varchar(255) DEFAULT NULL COMMENT '连接地址',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `level` int(11) DEFAULT NULL COMMENT '层级',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `id` bigint(20) NOT NULL COMMENT '日志主键',
  `type` tinyint(2) unsigned NOT NULL COMMENT '日志类型',
  `action` varchar(255) DEFAULT '' COMMENT '动作',
  `create_user_id` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `ip` varchar(32) DEFAULT NULL COMMENT '操作人IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户标识',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求URI',
  `request_type` varchar(20) DEFAULT NULL COMMENT '请求类型',
  `method` varchar(100) DEFAULT NULL COMMENT '操作方法',
  `elapsed_time` int(8) DEFAULT NULL COMMENT '耗时ms',
  `params` text DEFAULT NULL  COMMENT '提交的数据',
  `exception` text DEFAULT NULL COMMENT '异常信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';

-- ----------------------------
-- Table structure for `post`
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` bigint(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `author_id` varchar(19) NOT NULL,
  `excerpt` varchar(350) DEFAULT NULL COMMENT '摘要',
  `content` mediumtext NOT NULL,
  `type` tinyint(2) unsigned NOT NULL,
  `category_id` bigint(20) unsigned DEFAULT NULL,
  `post_status` tinyint(1) NOT NULL,
  `comment_status` tinyint(1) NOT NULL,
  `ping_status` tinyint(1) NOT NULL,
  `comment_count` int(10) unsigned NOT NULL,
  `read_count` int(10) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ----------------------------
-- Table structure for `comment`
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL,
  `post_id` bigint(20) unsigned NOT NULL,
  `parent_id` bigint(20) unsigned DEFAULT NULL COMMENT '父级评论',
  `author_id` bigint(20) unsigned DEFAULT NULL,
  `author_name` varchar(25) NOT NULL DEFAULT '',
  `email` varchar(100) DEFAULT NULL,
  `url` varchar(200) DEFAULT NULL COMMENT '评论者网址',
  `ip` varchar(100) DEFAULT NULL COMMENT '评论者IP',
  `agent` varchar(255) DEFAULT NULL COMMENT '评论者客户端',
  `content` text NOT NULL COMMENT '评论内容',
  `status` tinyint(2) NOT NULL  COMMENT '评论状态',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- Table structure for `link`
-- ----------------------------
DROP TABLE IF EXISTS `link`;
CREATE TABLE `link` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `sort` int(11) unsigned NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `visible` tinyint(1) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链接表';

-- ----------------------------
-- Table structure for `category`
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL,
  `name` varchar(25) NOT NULL,
  `leftv` int(11) unsigned NOT NULL,
  `rightv` int(11) unsigned NOT NULL,
  `visible` tinyint(1) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='类别表';

-- ----------------------------
--  Table structure for `tag`
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL,
  `name` varchar(15) NOT NULL,
  `post_id` bigint(20) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ----------------------------
--  Table structure for `file`
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL DEFAULT '',
  `type` tinyint(2) NOT NULL COMMENT '类型',
  `post_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联的文章',
  `key` varchar(100) NOT NULL DEFAULT '',
  `desc` varchar(255) NOT NULL DEFAULT '' COMMENT '资源描述',
  `height` smallint(5) unsigned COMMENT '如果是图片类型存放图片的高度',
  `width` smallint(5) unsigned COMMENT '如果是图片类型存放图片的宽度',
  `create_user_id` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
  `update_user_id` bigint(20) unsigned DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件、图片等';
