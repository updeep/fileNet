# description

## package setup

0. new dist-dir fileNet-{currentDate}
1. copy fileNet-example to dist-dir
2. build: mvn clean package
3. copy target/.jar && copy libs to dist-dir
4. copy webapp to dist-dir
5. edit dist-dir/app/config/server.properties
compress finished

## directory name rule
```
example: fileNet-20200202
```

## dist structure

| key |desc
|-----|------
| /   |
|	├─ app/             | 程序内部资源，包含配置文件、GUI图片、帮助文档
|	├─ bin/             | 启动脚本，Linux/Windows的打开方式
|	├─ libs/            | 外部引用资源（maven打包后生成的jar包）
|	├─ logs/            | 日志文件夹
|	├─ webapp/          | 页面静态资源，包含文件上传本地存储files文件夹
|	├─ fileNet-boot.jar | 程序jar包


## open mode
- Linux
`
chmod -R 777 bin/
sh bin/startup.sh
`
- Windows
`
double click on bin/startup.bat
`
