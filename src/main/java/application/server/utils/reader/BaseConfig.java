package application.server.utils.reader;

/**
 * Base interface for all configuration implementations (filesystem, memory or classpath)
 * @author admin
 */
public abstract class BaseConfig {

    public static final String ROOT_APP     = "app";
    public static final String ROOT_WEBAPP  = "webapp";

    /**
     * app under dir.
     */
    public static final String CONF_DIR_NAME = "config";
    public static final String GUIS_DIR_NAME = "guires";
    public static final String HELP_DIR_NAME = "helper";

    /**
     * webapp under dir.
     */
    public static final String FILE_DIR_NAME = "files";


    public static final String SERVER_PROPERTIES_FILE = "server.properties";

    public static final String DEFAULT_SETTING = "DEFAULT";
    public static final int DEFAULT_BUFFER_SIZE = 1048576;
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_LOG_LEVEL = "E";
    public static final String DEFAULT_VC_LEVEL = "STANDARD";
    public static final String DEFAULT_MUST_LOGIN = "O";

    /**
     * 状态码
     */
    public static final int LEGAL_PROPERTIES = 0;
    public static final int INVALID_PORT = 1;
    public static final int INVALID_LOG = 2;
    public static final int INVALID_FILE_SYSTEM_PATH = 3;
    public static final int INVALID_BUFFER_SIZE = 4;
    public static final int CANNOT_CREATE_FILE_BLOCK_PATH = 5;
    public static final int CANNOT_CREATE_FILE_NODE_PATH = 6;
    public static final int CANNOT_CREATE_TF_PATH = 7;
    public static final int CANNOT_CONNECT_DB = 8;
    public static final int HTTPS_SETTING_ERROR = 9;
    public static final int INVALID_VC = 10;
}
