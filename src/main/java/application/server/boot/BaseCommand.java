package application.server.boot;

/**
 * @author devcp
 */
public abstract class BaseCommand {

    public static final String DASH_LINE = "-";
    public static final String C_CONSOLE = "console";
    public static final String C_GUI = "gui";
    public static final String C_START = "start";
    public static final String C_STOP = "stop";
    public static final String C_RESTART = "restart";
    public static final String C_STATUS = "status";
    public static final String C_EXIT = "exit";
    public static final String C_HELP = "help";

    /**
     * 带短线的命令
     */
    public static final String DASH_C_CONSOLE = DASH_LINE + C_CONSOLE;
    public static final String DASH_C_GUI = DASH_LINE + C_GUI;
    public static final String DASH_C_START = DASH_LINE + C_START;
    public static final String DASH_C_STOP = DASH_LINE + C_STOP;
    public static final String DASH_C_RESTART = DASH_LINE + C_RESTART;
    public static final String DASH_C_STATUS = DASH_LINE + C_STATUS;
    public static final String DASH_C_EXIT = DASH_LINE + C_EXIT;
    public static final String DASH_C_HELP = DASH_LINE + C_HELP;

}
