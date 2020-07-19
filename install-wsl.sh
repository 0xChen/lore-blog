#!/bin/bash
# -------------------------------------------------
# author: syc
# 初始化 wsl 运行环境
# -------------------------------------------------

currentPath=`pwd`
appPath="/opt/lore-blog"
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
cp $currentPath/conf/mysql.cnf $appPath/mysql/conf/
chown -R 999:999 mysql

mkdir -p webapp/file
mkdir -p webapp/log
mkdir -p webapp/lock

mkdir -p nginx/conf
mkdir -p nginx/ssl
mkdir -p nginx/log
cp $currentPath/conf/nginx-localhost.conf $appPath/nginx/conf/nginx.conf
