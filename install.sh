#!/bin/bash
# -------------------------------------------------
# author: syc
# 目前只负责创建程序目录及配置文件, 后续会逐渐增加其它构建步骤
# -------------------------------------------------

appPath="/opt/lore-blog-test"
if  [ ! -d $appPath ];then
  mkdir -p $appPath
else
  echo "The folder '$appPath' already exists. In order to prevent incorrect installation, \
please delete the folder and reinstall."
  echo  "Installation failed."
  exit
fi
cd $appPath || exit

mkdir -p mysql/data
mkdir -p mysql/conf
mkdir -p mysql/log
mkdir -p mysql/log/bin
wget -P $appPath/mysql/conf https://github.com/0xChen/lore-blog/blob/master/conf/mysql.cnf
chown -R 999:999 mysql

mkdir -p webapp/file
mkdir -p webapp/log
mkdir -p webapp/lock

mkdir -p nginx/conf
mkdir -p nginx/ssl
mkdir -p nginx/log
wget -P $appPath/nginx/conf https://github.com/0xChen/lore-blog/blob/master/conf/nginx.conf
