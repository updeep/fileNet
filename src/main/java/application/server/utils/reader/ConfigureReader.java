package application.server.utils.reader;

import application.server.gui.printer.Printer;
import application.server.pojo.ServerSetting;
import application.server.pojo.enumeration.LogLevel;
import application.server.pojo.enumeration.VCLevel;
import application.server.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * <h2>Server配置解析器</h2>
 * <p>
 * 该工具负责读取并解析配置文件，并将结果随时提供给Server服务器业务逻辑以完成相应功能（例如用户认证、权限判定、配置启动端口等）。
 * </p>
 *
 * @author devcp
 * @version 1.0
 */
public class ConfigureReader extends BaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigureReader.class);

    /**
     * 自体实体
     */
    private static ConfigureReader cr;
    /**
     * 配置设置
     */
    public PropertiesUtils serverp;
    /**
     * 当前配置检查结果
     */
    private int propertiesStatus;
    private String path;
    private String appPath;
    private String webAppPath;

    private String confdir;
    private String mustLogin;
    private int port;
    private String log;
    private String vc;
    private int bufferSize;


    private final String DEFAULT_ROOT_APP_PATH;
    private final String DEFAULT_ROOT_WEBAPP_PATH;

    private boolean openHttps;
    private String httpsKeyFile;
    private String httpsKeyType;
    private String httpsKeyPass;
    private int httpsPort;

    private ConfigureReader() {
        this.propertiesStatus = -1;
        this.path = PathUtil.instance();
        this.DEFAULT_ROOT_APP_PATH = this.path + File.separator + BaseConfig.ROOT_APP + File.separator;
        this.DEFAULT_ROOT_WEBAPP_PATH = this.path + File.separator + BaseConfig.ROOT_WEBAPP + File.separator;
        this.confdir = this.DEFAULT_ROOT_APP_PATH + BaseConfig.CONF_DIR_NAME + File.separator;
        this.serverp = new PropertiesUtils();

        Printer.instance.print("Server Starting. Loading Configuration...");

        String serverPropPath = this.confdir + BaseConfig.SERVER_PROPERTIES_FILE;
        Printer.instance.print("Load ConfigFile:" + serverPropPath);

        final File serverProp = new File(serverPropPath);
        if (!serverProp.isFile()) {
            Printer.instance.print("服务器配置文件不存在，需要初始化服务器配置。");
            this.createDefaultServerPropertiesFile();
        }

        try {
            final FileInputStream serverPropIn = new FileInputStream(serverProp);
            this.serverp.load(serverPropIn);
            Printer.instance.print("配置文件载入完毕。正在检查配置...");
            this.propertiesStatus = this.checkServerPropertiesAndEffect(1);
            Printer.instance.print("当前配置检查结果，状态码=" + this.propertiesStatus);
            if (this.propertiesStatus == LEGAL_PROPERTIES) {
                Printer.instance.print("准备就绪。");
            }
        } catch (Exception e) {
            Printer.instance.print("错误：无法加载一个或多个配置文件（位于" + this.confdir + "路径下），请尝试删除旧的配置文件并重新启动本应用或查看安装路径的权限（必须可读写）。");
        }
    }

    public static ConfigureReader instance() {
        if (ConfigureReader.cr == null) {
            ConfigureReader.cr = new ConfigureReader();
        }
        return ConfigureReader.cr;
    }


    public int getBuffSize() {
        return this.bufferSize;
    }

    public String getInitBuffSize() {
        if (this.serverp != null && serverp.getProperty("buff.size") != null) {
            return serverp.getProperty("buff.size");
        } else {
            return DEFAULT_BUFFER_SIZE + "";
        }
    }

    public boolean inspectLogLevel(final LogLevel l) {
        int o = 0;
        int m = 0;
        if (l == null) {
            return false;
        }
        switch (l) {
            case None: {
                m = 0;
                break;
            }
            case Runtime_Exception: {
                m = 1;
            }
            case Event: {
                m = 2;
                break;
            }
            default: {
                m = 0;
                break;
            }
        }
        if (this.log == null) {
            this.log = "";
        }
        final String log = this.log;
        switch (log) {
            case "N": {
                o = 0;
                break;
            }
            case "R": {
                o = 1;
                break;
            }
            case "E": {
                o = 2;
                break;
            }
            default: {
                o = 1;
                break;
            }
        }
        return o >= m;
    }

    public boolean mustLogin() {
        return this.mustLogin != null && "N".equals(this.mustLogin);
    }

    public String getAppPath() {
        return this.appPath;
    }

    public String getInitAppPath() {
        if (this.serverp != null && serverp.getProperty("FS.path") != null) {
            return DEFAULT_SETTING.equals(serverp.getProperty("FS.path")) ? DEFAULT_ROOT_APP_PATH
                    : serverp.getProperty("FS.path");
        } else {
            return DEFAULT_ROOT_APP_PATH;
        }
    }

    public String getWebAppPath() {
        return webAppPath;
    }

    public String getInitWebAppPath() {
        return DEFAULT_ROOT_WEBAPP_PATH;
    }

    public String getPath() {
        return this.path;
    }

    public LogLevel getLogLevel() {
        if (this.log == null) {
            this.log = "";
        }
        final String log = this.log;
        switch (log) {
            case "N": {
                return LogLevel.None;
            }
            case "R": {
                return LogLevel.Runtime_Exception;
            }
            case "E": {
                return LogLevel.Event;
            }
            default: {
                return null;
            }
        }
    }

    public LogLevel getInitLogLevel() {
        if (serverp != null && serverp.getProperty("log") != null) {
            switch (serverp.getProperty("log")) {
                case "N": {
                    return LogLevel.None;
                }
                case "R": {
                    return LogLevel.Runtime_Exception;
                }
                case "E": {
                    return LogLevel.Event;
                }
                default:
                    return LogLevel.Event;
            }
        } else {
            return LogLevel.Event;
        }
    }

    /**
     * <h2>获得验证码等级</h2>
     * <p>
     * 返回设置的验证码等级枚举类（application.server.pojo.enumeration.VCLevel），包括：关闭（CLOSE）、简单（Simplified）、标准（Standard）
     * </p>
     *
     * @return application.server.pojo.enumeration.VCLevel 验证码等级
     * @author devcp
     */
    public VCLevel getVCLevel() {
        if (this.vc == null) {
            this.vc = "";
        }
        final String vc = this.vc;
        switch (vc) {
            case "STANDARD": {
                return VCLevel.Standard;
            }
            case "SIMP": {
                return VCLevel.Simplified;
            }
            case "CLOSE": {
                return VCLevel.Close;
            }
            default: {
                return null;
            }
        }
    }

    public VCLevel getInitVCLevel() {
        if (serverp != null && serverp.getProperty("VC.level") != null) {
            switch (serverp.getProperty("VC.level")) {
                case "STANDARD":
                    return VCLevel.Standard;
                case "SIMP":
                    return VCLevel.Simplified;
                case "CLOSE":
                    return VCLevel.Close;
                default:
                    return VCLevel.Standard;
            }
        } else {
            return VCLevel.Standard;
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getInitPort() {
        if (this.serverp != null && serverp.getProperty("port") != null) {
            return serverp.getProperty("port");
        } else {
            return DEFAULT_PORT + "";
        }
    }

    public int getPropertiesStatus() {
        return this.propertiesStatus;
    }

    /**
     * <h2>重新检查各项设置</h2>
     * <p>
     * 在服务器启动前再次检查各设置，实现某些设置的“即插即用”。
     * </p>
     *
     * @author devcp
     */
    public void checkServerPropertiesAndEffectRe() {
        this.propertiesStatus = checkServerPropertiesAndEffect(2);
    }

    public boolean doUpdate(final ServerSetting ss) {
        if (ss != null) {
            Printer.instance.print("正在更新服务器配置...");
            this.serverp.setProperty("mustLogin", ss.isMustLogin() ? "N" : "O");
            this.serverp.setProperty("buff.size", ss.getBuffSize() + "");
            String loglevelCode = "E";
            switch (ss.getLog()) {
                case Event: {
                    loglevelCode = "E";
                    break;
                }
                case Runtime_Exception: {
                    loglevelCode = "R";
                    break;
                }
                case None: {
                    loglevelCode = "N";
                    break;
                }
                default:
                    break;
            }
            this.serverp.setProperty("log", loglevelCode);
            switch (ss.getVc()) {
                case Standard: {
                    this.serverp.setProperty("VC.level", "STANDARD");
                    break;
                }
                case Close: {
                    this.serverp.setProperty("VC.level", "CLOSE");
                    break;
                }
                case Simplified: {
                    this.serverp.setProperty("VC.level", "SIMP");
                    break;
                }
                default:
                    break;
            }
            this.serverp.setProperty("port", ss.getPort() + "");
            this.serverp.setProperty("FS.path",
                    (ss.getFsPath() + File.separator).equals(this.DEFAULT_ROOT_APP_PATH) ? DEFAULT_SETTING
                            : ss.getFsPath());
            for (short i = 1; i < 32; i++) {
                // 清空旧的扩展存储区设置
                this.serverp.removeProperty("FS.extend." + i);
            }
            if (this.checkServerPropertiesAndEffect(2) == 0) {
                try {
                    this.serverp.store(new FileOutputStream(this.confdir + BaseConfig.SERVER_PROPERTIES_FILE), null);
                    Printer.instance.print("配置更新完毕，准备就绪。");
                    return true;
                } catch (Exception e) {
                    Printer.instance.print("错误：更新设置失败，无法存入设置文件。");
                }
            }
        }
        return false;
    }

    /**
     * <h2>验证配置并完成赋值</h2>
     * <p>
     * 该方法用于对配置文件进行验证并将正确的值赋予相应的属性，必须在构造器中执行本方法。
     * </p>
     *
     * @return int 验证结果代码
     * @author devcp
     */
    private int checkServerPropertiesAndEffect(int seqNumber) {
        if (seqNumber == 1) {
            Printer.instance.print("正在检查服务器配置...");
        }

        if (checkPort() != LEGAL_PROPERTIES) {
            return INVALID_PORT;
        }

        this.mustLogin = this.serverp.getProperty("mustLogin");
        if (this.mustLogin == null) {
            Printer.instance.print("警告：未找到是否必须登录配置，将采用默认值（O）。");
            this.mustLogin = "O";
        }
        final String logs = this.serverp.getProperty("log");
        if (logs == null) {
            Printer.instance.print("警告：未找到日志等级配置，将采用默认值（E）。");
            this.log = "E";
        } else {
            if (!"N".equals(logs) && !"R".equals(logs) && !"E".equals(logs)) {
                return INVALID_LOG;
            }
            this.log = logs;
        }
        final String vcl = this.serverp.getProperty("VC.level");
        if (vcl == null) {
            Printer.instance.print("警告：未找到登录验证码配置，将采用默认值（STANDARD）。");
            this.vc = DEFAULT_VC_LEVEL;
        } else {
            switch (vcl) {
                case "STANDARD":
                case "SIMP":
                case "CLOSE":
                    this.vc = vcl;
                    break;
                default:
                    return INVALID_VC;
            }
        }

        this.appPath = getInitAppPath();
        if (!appPath.endsWith(File.separator)) {
            appPath = appPath + File.separator;
        }
        final File fsFile = new File(this.appPath);
        if (!fsFile.isDirectory() || !fsFile.canRead() || !fsFile.canWrite()) {
            Printer.instance.print("错误：app路径[" + this.appPath + "]无效，该路径必须指向一个具备读写权限的文件夹。");
            return INVALID_FILE_SYSTEM_PATH;
        }

        this.webAppPath = getInitWebAppPath();
        final File webappFile = new File(this.webAppPath);
        if (!webappFile.isDirectory() || !webappFile.canRead() || !webappFile.canWrite()) {
            Printer.instance.print("错误：webapp路径[" + this.webAppPath + "]无效，该路径必须指向一个具备读写权限的文件夹。");
            return INVALID_FILE_SYSTEM_PATH;
        }


        if (checkBufferSize() != LEGAL_PROPERTIES) {
            return INVALID_BUFFER_SIZE;
        }

        // https支持检查及生效处理
        if (checkHttps() != LEGAL_PROPERTIES) {
            return BaseConfig.HTTPS_SETTING_ERROR;
        }
        if (seqNumber == 1) {
            Printer.instance.print("检查完毕。");
        }
        return LEGAL_PROPERTIES;
    }

    private int checkPort() {
        final String ports = this.serverp.getProperty("port");
        if (ports == null) {
            Printer.instance.print("警告：未找到端口配置，将采用默认值（8080）。");
            this.port = 8080;
        } else {
            try {
                this.port = Integer.parseInt(ports);
                if (this.port <= 0 || this.port > 65535) {
                    Printer.instance.print("错误：端口号配置不正确，必须使用1-65535之间的整数。");
                    return INVALID_PORT;
                }
            } catch (Exception e) {
                Printer.instance.print("错误：端口号配置不正确，必须使用1-65535之间的整数。");
                return INVALID_PORT;
            }
        }
        return LEGAL_PROPERTIES;
    }

    private int checkBufferSize() {
        final String bufferSizes = this.serverp.getProperty("buff.size");
        if (bufferSizes == null) {
            Printer.instance.print("警告：未找到缓冲大小配置，将采用默认值（1048576）。");
            this.bufferSize = 1048576;
        } else {
            try {
                this.bufferSize = Integer.parseInt(bufferSizes);
                if (this.bufferSize <= 0) {
                    Printer.instance.print("错误：缓冲区大小设置无效。");
                    return INVALID_BUFFER_SIZE;
                }
            } catch (Exception e2) {
                Printer.instance.print("错误：缓冲区大小设置无效。");
                return INVALID_BUFFER_SIZE;
            }
        }
        return LEGAL_PROPERTIES;
    }

    private int checkHttps() {
        if ("true".equals(serverp.getProperty("https.enable"))) {
            File keyFile = new File(path, "https.p12");
            if (keyFile.isFile()) {
                httpsKeyType = "PKCS12";
            } else {
                keyFile = new File(path, "https.jks");
                if (keyFile.isFile()) {
                    httpsKeyType = "JKS";
                } else {
                    Printer.instance.print(
                            "错误：无法启用https支持，因为Server未能找到https证书文件。您必须在应用主目录内放置PKCS12（必须命名为https.p12）或JKS（必须命名为https.jks）证书。");
                    return BaseConfig.HTTPS_SETTING_ERROR;
                }
            }
            httpsKeyFile = keyFile.getAbsolutePath();
            httpsKeyPass = serverp.getProperty("https.keypass", "");
            String httpsports = serverp.getProperty("https.port");
            if (httpsports == null) {
                Printer.instance.print("警告：未找到https端口配置，将采用默认值（443）。");
                httpsPort = 443;
            } else {
                try {
                    this.httpsPort = Integer.parseInt(httpsports);
                    if (httpsPort <= 0 || httpsPort > 65535) {
                        Printer.instance.print("错误：无法启用https支持，https访问端口号配置不正确。");
                        return BaseConfig.HTTPS_SETTING_ERROR;
                    }
                } catch (Exception e) {
                    Printer.instance.print("错误：无法启用https支持，https访问端口号配置不正确。");
                    return BaseConfig.HTTPS_SETTING_ERROR;
                }
            }
            openHttps = true;
        }
        return LEGAL_PROPERTIES;
    }

    public void createDefaultServerPropertiesFile() {
        Printer.instance.print("正在生成初始服务器配置文件（" + this.confdir + BaseConfig.SERVER_PROPERTIES_FILE + "）...");
        final Properties dsp = new Properties();
        dsp.setProperty("mustLogin", DEFAULT_MUST_LOGIN);
        dsp.setProperty("port", DEFAULT_PORT + "");
        dsp.setProperty("log", DEFAULT_LOG_LEVEL);
        dsp.setProperty("VC.level", DEFAULT_VC_LEVEL);
        dsp.setProperty("FS.path", DEFAULT_SETTING);
        dsp.setProperty("buff.size", DEFAULT_BUFFER_SIZE + "");
        try {
            dsp.store(new FileOutputStream(this.confdir + BaseConfig.SERVER_PROPERTIES_FILE),
                    "# This is the default server configuration setting.");
            Printer.instance.print("初始服务器配置文件生成完毕。");
        } catch (FileNotFoundException e) {
            Printer.instance.print("错误：无法生成初始服务器配置文件，存储路径不存在。");
        } catch (IOException e2) {
            Printer.instance.print("错误：无法生成初始服务器配置文件，写入失败。");
        }
    }

    /**
     * <h2>是否开启Https支持</h2>
     * <p>
     * 该方法将返回用户是否开启了https的设置项。
     * </p>
     *
     * @return boolean 是否开启
     * @author devcp
     */
    public boolean openHttps() {
        return openHttps;
    }

    public String getHttpsKeyType() {
        return httpsKeyType;
    }

    public String getHttpsKeyFile() {
        return httpsKeyFile;
    }

    public String getHttpsKeyPass() {
        return httpsKeyPass;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

}
