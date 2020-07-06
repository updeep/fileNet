# fileNet 前端框架源码
一款基于AngularJS技术栈的响应式文件浏览管理器

基于开源框架重构扩展，[angular-filemanager](https://github.com/joni2back/angular-filemanager)

## 快速上手
```
- 编译需要用到node.js和gulp模块
- 先安装全局gulp模块 npm install -g gulp
- 然后在项目根目录执行npm install
- 最后打包编译到dist目录下 gulp build
```
- gulp安装，官网推荐：https://www.gulpjs.com.cn/
```
npm install gulp-cli -g
npm install gulp -D
npx -p touch nodetouch gulpfile.js
gulp --help
``` 
- 打包命令`
gulp build
`

## 项目结构
```
|- dist 打包编译好的压缩文件
|- lib  整理好的第三方依赖包，配合源码/压缩文件可直接使用
|- src  源代码目录
index.html 项目访问入口文件，启动后直接ip:port访问
```

## 功能介绍
```
前后端分离,方便集成到自己的熟悉技术项目中
支持选择回调,如弹框文件选择
多语言支持
支持多种文件列表布局（图标/详细列表）
多文件上传
支持文件搜索
复制、移动、重命名
删除、修改、预览、下载
直接压缩、解压缩zip文件(支持解压zip、tar.gz、rar)
支持设置文件权限(UNIX chmod格式)
移动端支持
支持office等文档在线预览(原生HTML，非转码成PDF)
```

- 关于office等文档在线预览
```
新版支持office 等文档在线预览和(编辑)，主要还是感谢onlyoffice这个产品，目前自己部署的话是免费滴。 具体配置方式如下:

部署好onlyoffice，建议是用Docker方式，可自行百度，这里不在重复写教程了

部署好以后，把application.properties里的files.docservice.url.api配置成onlyofficeAPI的地址，以及需要把fileServer.domain配置成文件管理服务所在的全域名

fileServer.domain配置千万不能配置127或者local等，原因是docker中的onlyoffice如果会访问到本机
```
