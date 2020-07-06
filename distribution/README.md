# description

## package setup

1. $: mvn clean compile package

2. copy jar to new fileNet-v{currentDate}

3. compress finished


## directory name rule
```
example: fileNet-v2020.0313
explain: appName-v{currentDate}
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
|	├─ file-service.jar | 程序jar包


## open mode
- Linux
`
sh bin/fileNet.sh
`
- Windows
`
double click on bin/fileNet.bat
`