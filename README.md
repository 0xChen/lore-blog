# Lore-Blog 博客系统
## 简介
 `Lore`是一个主要定位于像作者这样热衷折腾, 并在学习和使用[Spring](https://spring.io)技术栈的你而开发的单用户博客系统. 
 如果你只是单纯的在寻找一个小而美并专注发表文章的系统那么它并不适合你.
 > lore	[lɔː(r)] n. 学问; 知识;  
 希望这个博客系统能够让使用者: 学到学识, 记录学识, 传播学识.
 
[![License](https://img.shields.io/badge/license-MIT-4EB1BA.svg?style=flat-square)](https://github.com/sssyyyccc/lore-blog/blob/dev/LICENSE)

## 主要技术

+ Spring Boot
+ Spring Security
+ Spring Framework
+ Mybatis-plus
+ Thymeleaf

## 部分页面预览

### 首页
![首页](https://i.loli.net/2019/06/06/5cf8a296a430286044.png)
### 文章正文
![文章正文](https://i.loli.net/2019/06/06/5cf8b02e3e66822364.png)
### 安装页面
![安装页面](https://i.loli.net/2019/08/21/QwEJbxjM1RpOnlZ.jpg)
### 防止重复安装
![防止重复安装](https://i.loli.net/2019/08/21/IdMlxYv8w73fceu.jpg)
### 文章编辑页面
![文章编辑页面](https://i.loli.net/2019/08/23/EzncHbGxgTY6ADM.png)
### 编辑关于页面
![关于页面](https://i.loli.net/2019/08/23/F5qvuphyJYdeEsP.png)

## 说明
系统还不是很完善, 还有很多我个人不满意的地方在逐步完善中, 暂时关闭 Issues 功能. 作者联系方式Email: sunyuchen1990@gmail.com

## 安装说明
运行docker-compose.yml前先将"mysql-chown.sh"文件放入"/opt/lore-blog/init.sh/"路径下,
否则mysql的docker容器因为权限问题无法启动. 或者在运行docker-compose.yml前手动创建目录"/opt/lore-blog/mysql/log",
并执行命令chown -R 999:999 /opt/lore-blog/mysql/log

## 致谢

* [tale](https://github.com/otale/tale) 后台管理主题来源
* [pinghsu](https://github.com/chakhsu/pinghsu) 博客默认主题来源
