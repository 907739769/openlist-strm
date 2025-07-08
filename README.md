# openlist-strm
 openlist生成可播放strm视频文件

## 主要功能

```
1.生成strm文件，启动即执行，定时任务执行，tg机器人执行，接口调用执行
2.复制同步openlist的两个文件夹，tg机器人执行，接口调用执行
3.可支持第三方app回调，自动化处理，如qb下载完成通知，自动复制openlist挂载的本地硬盘复制到云盘然后生成strm文件
```

## docker部署

```
部署前参数需要修改
必要参数
openlistServerUrl  openlist地址 如http://192.168.1.2:5244
openlistServerToken 可在openlist后台获取
openlistScanPath 需要生成strm文件的目录如http://192.168.1.2:5244/阿里云分享/电影 那就填入/阿里云分享/电影  支持多目录用,分隔
可选参数
slowMode  单线程模式，防止请求网盘太快，默认0，启用填1
encode 是否编码strm文件里面的链接  默认1启用  不启用填0
isDownSub 是否下载目录里面的字幕文件 默认0不下载  下载填1
runAfterStartup  启动是否立即执行同步任务 默认启用1，启用填0
logLevel 日志级别 DEBUG INFO ERROR OFF
tgToken  tg机器人token，通过t.me/BotFather机器人创建bot获取token
tgUserId tg用户id，通过t.me/userinfobot机器人获取userId
maxIdleConnections HTTP调用线程池参数配置 默认5
refresh参数  是否去读取网盘最新数据，1是实时读取网盘 0是读取openlist缓存 默认1
PUID  用户参数  生成strm文件的所属用户
PGID  用户组参数 生成strm文件的所属用户组

复制openlist不同目录的视频 源目录删除不会删除目标目录文件 只会新增
srcDir 源目录
dstDir 目标目录
minFileSize 复制的最小文件
replaceDir qb的下载根目录 使用/api/v1/notifyByDir接口时需要填
strmAfterSync参数，支持上传完文件不生成strm  默认1生成strm  0不生成strm
scheduledCron 定时任务cron参数，默认0 0 6,18 * * ?  每天6点和18点执行同步任务

```

# 开发计划

- [x] tg机器人命令生成strm文件
- [ ] ...

# 更新记录

```
20240610 重构代码,增加tg机器人命令strm、strmdir
20240617 增加下载目录中字幕文件的功能
20240617 增加openlist目录复制的功能 使用tg机器人/sync命令执行任务
20240622 执行sync任务之后自动执行strm任务 增加定时任务每天6、18点执行sync任务
20240623 增加调用接口api/v1/notify直接执行复制sync任务  配合qb使用  监听端口是6894
20240624 增加/syncdir命令执行指定目录的同步复制，如：/sync /阿里云盘/电影#/115网盘/电影，就会将/阿里云盘/电影下的视频同步复制到/115网盘/电影
20240630 增加参数minFileSize 默认100  判断视频大小是否大于100MB，如果大于100MB才复制同步文件
20240724 增加参数配置日志级别 增加复制任务多线程执行
20240807 增加HTTP调用线程池参数配置maxIdleConnections 默认5
20240821 增加refresh参数  是否去读取网盘最新数据，1是实时读取网盘 0是读取openlist缓存 默认1
20241107 增加/api/v1/notifyByDir接口和replaceDir参数，按需同步目录，防止同步文件太多，耗时过长
20241202 增加定时任务cron参数scheduledCron，默认0 0 6,18 * * ?  每天6点和18点执行同步任务 
20250108 优化日志打印
20250111 复制任务完成之后立即生成strm文件，不用等待所有复制任务完成，自动重试失败的复制任务
20250115 增加strmAfterSync参数，支持上传完文件不生成strm  默认1生成strm  0不生成strm
20250424 优化性能 
```

# docker CLI安装

```
docker run -d \
--name=openlist-strm \
-e TZ=Asia/Shanghai \
-e openlistServerUrl=http://192.168.1.2:5244 \
-e openlistServerToken=xxx \
-e openlistScanPath='/阿里云分享/电影' \
-e slowMode=0 \
-v /volume1/docker/openlist-strm/data:/data \
jacksaoding/openlist-strm:latest
```

# docker compose安装

```
version: "3"
services:
  app:
    container_name: openlist-strm
    image: 'jacksaoding/openlist-strm:latest'
    network_mode: "host"
    environment:
      TZ: Asia/Shanghai
      openlistServerUrl: http://192.168.1.2:5244
      openlistServerToken: xxx
      openlistScanPath: /阿里云分享/电影
      slowMode: 0
    volumes:
      - /volume1/docker/openlist-strm/data:/data
```

# qb脚本参考

`sh /config/notify.sh "%G" "%F"`

```
#!/bin/bash

# 获取传递的标签
TAG=$1
dir=$2
MOVIEPILOT="MOVIEPILOT"

if [[ "$TAG" =~ "$MOVIEPILOT" ]]; then
  # 调用 notify 接口
  #curl -X POST http://192.168.31.66:6894/api/v1/notify
  curl -X POST -H "Content-Type: application/json" -d "{\"dir\": \"$dir\"}" http://192.168.31.66:6894/api/v1/notifyByDir &>/dev/null &
fi
```
