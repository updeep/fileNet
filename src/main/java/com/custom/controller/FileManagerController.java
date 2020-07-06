package com.custom.controller;

import application.server.utils.reader.ConfigureReader;
import application.server.utils.reader.IConst;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.custom.component.filemanager.utils.RarUtils;
import com.custom.component.filemanager.utils.TargzUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author devcp
 */
@RestController
@RequestMapping("/api/fileManager")
public class FileManagerController {

    /**
     * log4j日志记录器
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 文件管理器根目录
     */
    private String root = ConfigureReader.instance().serverp.getProperty(IConst.FILE_ROOT_PATH);

    /**
     * 展示文件列表
     */
    @RequestMapping(value = "/list", method = {RequestMethod.POST, RequestMethod.GET})
    public Object list(@RequestBody JSONObject json) throws ServletException {

        try {
            // 需要显示的目录路径
            String path = json.getString("path");

            // 返回的结果集
            List<JSONObject> fileItems = new ArrayList<>();

            Path dirPath = init(root, path);
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {

                SimpleDateFormat dt = new SimpleDateFormat(IConst.DATE_FORMAT);
                for (Path pathObj : directoryStream) {
                    // 获取文件基本属性
                    BasicFileAttributes attrs = Files.readAttributes(pathObj, BasicFileAttributes.class);

                    // 封装返回JSON数据
                    JSONObject fileItem = new JSONObject();
                    fileItem.put("name", pathObj.getFileName().toString());

                    // windows 下这句话会影响权限导致无法读取文件？ 待验证 目前先注释掉 -- 文件权限
                    //fileItem.put("rights", com.custom.component.filemanager.utils.FileUtils.getPermissions(pathObj));

                    fileItem.put("date", dt.format(new Date(attrs.lastModifiedTime().toMillis())));
                    fileItem.put("size", attrs.size());
                    fileItem.put("type", attrs.isDirectory() ? "dir" : "file");
                    fileItems.add(fileItem);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", fileItems);
            return jsonObject;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 初始化，判断上传目录是否存在，不存在就创建
     *
     * @param root
     * @return
     */
    public Path init(String root, String path) {
        Path dirPath = Paths.get(root, path);
        try {
            if (Files.notExists(dirPath)) {

                // os support windows linux mac
                String os = System.getProperties().getProperty("os.name");
                if (os != null && os.toLowerCase().contains(IConst.OS_WINDOWS)) {
                    logger.info("os is windows");
                } else {
                    logger.info("os is linux or mac");
                }

                //创建文件目录
                Path createPath = Files.createDirectories(dirPath);
                logger.info("init createDirectories - " + createPath.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirPath;
    }

    /**
     * 文件上传
     */
    @RequestMapping(value = "/upload", method = {RequestMethod.POST})
    public Object upload(HttpServletRequest request) {
        try {
            // 配置上传参数
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 解析请求的内容提取文件数据
            List<FileItem> formItems = upload.parseRequest(request);

            // 获取表单中的字段
            String destination = "";
            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    if ("destination".equals(item.getFieldName())) {
                        destination = item.getString();
                    }
                }
            }
            // 处理不在表单中的字段
            for (FileItem item : formItems) {
                if (!item.isFormField()) {
                    // 处理不在表单中的字段
                    String fileName = item.getName();
                    //定义上传文件的存放路径
                    String path = root + destination;
                    //定义上传文件的完整路径
                    String filePath = String.format("%s/%s", path, fileName);
                    File storeFile = new File(filePath);
                    // 在控制台输出文件的上传路径
                    logger.info("upload.to - " + filePath);
                    // 保存文件到硬盘
                    item.write(storeFile);
                }
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 文件下载/预览
     */
    @RequestMapping(value = "/preview", method = {RequestMethod.POST, RequestMethod.GET})
    public void preview(HttpServletResponse response, String path) throws IOException {

        File file = new File(root, path);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
            return;
        }

        /*
         * 获取mimeType
         */
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(file.getName(), "UTF-8")));
        response.setContentLength((int) file.length());

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 创建目录
     */
    @RequestMapping(value = "/createFolder", method = {RequestMethod.POST, RequestMethod.GET})
    public Object createFolder(@RequestBody JSONObject json) {
        try {
            String newPath = json.getString("newPath");
            File newDir = new File(root + newPath);
            if (!newDir.mkdir()) {
                throw new Exception("不能创建目录: " + newPath);
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 修改文件或目录权限
     */
    @RequestMapping(value = "/changePermissions", method = {RequestMethod.POST, RequestMethod.GET})
    public Object changePermissions(@RequestBody JSONObject json) {
        try {
            // 权限
            String perms = json.getString("perms");
            // 子目录是否生效
            boolean recursive = json.getBoolean("recursive");

            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                String path = items.getString(i);
                File f = new File(root, path);
                // 设置权限
                com.custom.component.filemanager.utils.FileUtils.setPermissions(f, perms, recursive);
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 复制文件或目录
     */
    @RequestMapping(value = "/copy", method = {RequestMethod.POST, RequestMethod.GET})
    public Object copy(@RequestBody JSONObject json, HttpServletRequest request) {
        try {
            String newpath = json.getString("newPath");
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.size(); i++) {
                String path = items.getString(i);

                File srcFile = new File(root, path);
                File destFile = new File(root + newpath, srcFile.getName());

                FileCopyUtils.copy(srcFile, destFile);
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 移动文件或目录
     */
    @RequestMapping(value = "/move", method = {RequestMethod.POST, RequestMethod.GET})
    public Object move(@RequestBody JSONObject json) {
        try {
            String newpath = json.getString("newPath");
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.size(); i++) {
                String path = items.getString(i);

                File srcFile = new File(root, path);
                File destFile = new File(root + newpath, srcFile.getName());

                if (srcFile.isFile()) {
                    org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
                } else {
                    org.apache.commons.io.FileUtils.moveDirectory(srcFile, destFile);
                }
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 删除文件或目录
     */
    @RequestMapping(value = "/remove", method = {RequestMethod.POST, RequestMethod.GET})
    public Object remove(@RequestBody JSONObject json) {
        try {
            JSONArray items = json.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                String path = items.getString(i);
                File srcFile = new File(root, path);
                if (!org.apache.commons.io.FileUtils.deleteQuietly(srcFile)) {
                    throw new Exception("删除失败: " + srcFile.getAbsolutePath());
                }
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 重命名文件或目录
     */
    @RequestMapping(value = "/rename", method = {RequestMethod.POST, RequestMethod.GET})
    public Object rename(@RequestBody JSONObject json) {
        try {
            String path = json.getString("item");
            String newPath = json.getString("newItemPath");

            File srcFile = new File(root, path);
            File destFile = new File(root, newPath);
            if (srcFile.isFile()) {
                org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
            } else {
                org.apache.commons.io.FileUtils.moveDirectory(srcFile, destFile);
            }
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 查看文件内容,针对html、txt等可编辑文件
     */
    @RequestMapping(value = "/getContent", method = {RequestMethod.POST, RequestMethod.GET})
    public Object getContent(@RequestBody JSONObject json) {
        try {
            String path = json.getString("item");
            File srcFile = new File(root, path);

            String content = org.apache.commons.io.FileUtils.readFileToString(srcFile);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", content);
            return jsonObject;
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 修改文件内容,针对html、txt等可编辑文件
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.POST, RequestMethod.GET})
    public Object edit(@RequestBody JSONObject json) {
        try {
            String path = json.getString("item");
            String content = json.getString("content");

            File srcFile = new File(root, path);
            org.apache.commons.io.FileUtils.writeStringToFile(srcFile, content);

            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 文件压缩
     */
    @RequestMapping(value = "/compress", method = {RequestMethod.POST, RequestMethod.GET})
    public Object compress(@RequestBody JSONObject json) {
        try {
            String compressedFilename = json.getString("compressedFilename");
            String destination = json.getString("destination");
            JSONArray items = json.getJSONArray("items");
            List<File> files = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                File f = new File(root, items.getString(i));
                files.add(f);
            }
            if (compressedFilename.indexOf(".zip") < 0) {
                compressedFilename += ".zip";
            }
            File zipFile = new File(root + destination, compressedFilename);
            System.out.println("compress file:" + zipFile.getPath());

            ZipUtil.zip(zipFile, true, files.toArray(new File[files.size()]));
            return success();
        } catch (Exception e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }

    /**
     * 文件解压
     */
    @RequestMapping(value = "/extract", method = {RequestMethod.POST, RequestMethod.GET})
    public Object extract(@RequestBody JSONObject json) {
        try {
            String folderName = json.getString("folderName");
            String destination = json.getString("destination");
            String item = json.getString("item");
            String outDir = root + destination;
            File file = new File(root, item);
            System.out.println("extract file:" + file.getPath());

            String extension = com.custom.component.filemanager.utils.FileUtils.getExtension(item);
            switch (extension) {
                case ".zip":
                    ZipUtil.unzip(file, new File(outDir, folderName), Charset.forName("GBK"));
                    break;
                case ".rar":
                    RarUtils.unRarFile(file, outDir + File.separator + folderName);
                    break;
                case ".gz":
                    TargzUtils.unTargzFile(file, outDir + File.separator + folderName);
                    break;
                default:
                    // break;
                    return error(extension + "不支持的格式");
            }
            return success();
        } catch (Exception e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }


    private JSONObject error(String msg) {

        // { "result": { "success": false, "error": "msg" } }
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("error", msg);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", result);
        return jsonObject;

    }

    private JSONObject success() {
        // { "result": { "success": true, "error": null } }
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("error", null);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", result);
        return jsonObject;
    }
}
