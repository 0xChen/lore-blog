# Lore-Blog 博客系统
          __       __
         /\ \     /\ \
         \ \ \____\ \ \        ___      __
          \ \ '__`\\ \ \  __  / __`\  /'_ `\
           \ \ \L\ \\ \ \O\ \/\ \R\ \/\ \E\ \
            \ \____/ \ \____/\ \____/\ \____ \
             \/___/   \/___/  \/___/  \/____\ \
                                        /\____/
                                        \_/__/
                                        
                                         <> with ❤ by SYC
## 简介
 `Lore`是一个主要定位于像作者这样热衷折腾, 并在学习和使用[Spring](https://spring.io)技术栈的你而开发的单用户博客系统. 
 如果你只是单纯的在寻找一个小而美并专注发表文章的系统那么它并不适合你.
 > lore	[lɔː(r)] n. 学问; 知识;  
 希望这个博客系统能够让使用者: 学到学识, 记录学识, 传播学识.
 
[![License](https://img.shields.io/badge/license-MIT-4EB1BA.svg?style=flat-square)](https://github.com/0xChen/lore-blog/blob/dev/LICENSE)
[![code with hearth by SYC](https://img.shields.io/badge/%3C%3E%20with%20%E2%99%A5%20by-SYC-3caa62.svg)](https://github.com/0xChen)
## 主要技术

+ Spring Boot
+ Spring Security
+ Spring Framework
+ Mybatis-plus
+ Thymeleaf

## 部分页面预览

### 首页
![首页](https://i.loli.net/2019/08/23/jKF1MyEzf6kle4Q.png)
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

## 简要安装说明
### 使用idea编译器远程连接服务器Docker安装
1. 将项目根目录的install.sh文件上传到服务器并执行
2. build项目后在idea中执行docker-compose.yml

### 直接在服务器上安装
1. 自行在服务器上安装好Docker及Docker Compose. 
2. 将项目中的install.sh, Dockerfile, docker-compose.yml, 项目编译后的jar
及 jwtSecretKey, jwtSecretKey(这两个密钥文件是自行制作的)全部上传到服务器
修改Dockerfile文件的15行中的'./build/libs/*.jar' 为jar文件在服务器的实际
位置. 
3. 分别执行install.sh, Dockerfile及docker-compose.yml

## 致谢

* [tale](https://github.com/otale/tale) 后台管理主题来源
* [pinghsu](https://github.com/chakhsu/pinghsu) 博客默认主题来源
