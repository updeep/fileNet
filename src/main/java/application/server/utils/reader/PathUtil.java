package application.server.utils.reader;

import java.io.File;

public class PathUtil {

    public static String instance() {
        // 开发环境下
        String path = System.getProperty("user.dir");
        String classPath = System.getProperty("java.class.path");
        if (classPath.indexOf(File.pathSeparator) < 0) {
            File f = new File(classPath);
            classPath = f.getAbsolutePath();
            if (classPath.endsWith(".jar")) {
                // 使用环境下
                path = classPath.substring(0, classPath.lastIndexOf(File.separator));
            }
        }
        return path;
    }
}
