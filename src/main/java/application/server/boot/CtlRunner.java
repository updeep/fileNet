package application.server.boot;

import application.server.utils.reader.ConfigureReader;
import application.server.utils.reader.BaseConfig;
import application.server.gui.printer.Printer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * <h2>命令模式启动器</h2>
 * <p>
 * 该启动器将以命令模式启动服务，请执行静态build()方法开启界面并初始化服务引擎。
 * </p>
 *
 * @author devcp
 * @version 1.0
 */
public class CtlRunner extends BaseCommand {
    private static CtlRunner cs;
    private static AppCtl ctl;
    private static String commandTips;
    private Scanner reader;

    private Executor worker;

    /**
     * need to change!阿里巴巴的规范中：不允许使用Executors来创建线程池。
     * worker = Executors.newSingleThreadExecutor();
     * <p>
     * ————————————————
     * 创建固定大小的线程池，可以延迟或定时的执行任务
     * 原文链接：https://blog.csdn.net/weixin_41888813/article/details/90769126
     */
    private CtlRunner() {
        Printer.init(false);
        CtlRunner.ctl = new AppCtl();

        worker = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("t1-schedule-pool-%d").daemon(true).build());

        CtlRunner.commandTips = "提示:您可以输入以下指令以控制服务器：\r\n" +
                "-start 启动服务器\r\n-stop 停止服务器\r\n-exit 停止服务器并退出应用\r\n-restart 重启服务器\r\n-status 查看服务器状态\r\n-help 显示帮助文本";
    }

    /**
     * <h2>以命令模式运行</h2>
     * <p>
     * 启动命令模式操作并初始化服务器引擎，该方法将返回本启动器的唯一实例。
     * </p>
     *
     * @param args java.lang.String[] 启动参数
     * @return application.handler.CtlRunner 本启动器唯一实例
     * @author devcp
     */
    public static CtlRunner build(final String[] args) {
        if (CtlRunner.cs == null) {
            CtlRunner.cs = new CtlRunner();
        }
        CtlRunner.cs.execute(args);
        return CtlRunner.cs;
    }

    /**
     * 执行相应的指令并进行后续处理，该方法为整个命令模式的起点。
     *
     * @param args
     */
    private void execute(final String[] args) {
        if (args.length > 0) {
            final String command = args[0];
            switch (command) {
                case DASH_C_CONSOLE: {
                    this.startByConsole();
                    break;
                }
                case DASH_C_START: {
                    CtlRunner.ctl.start();
                    break;
                }
                default: {
                    Printer.instance
                            .print("提示:无效的指令，使用控制台模式启动请输入参数 -console，备份文件请输入参数 -backup {本地备份路径}，使用GUI模式启动请不传入任何参数。");
                    break;
                }
            }
        }
    }

    private void startByConsole() {
        Printer.instance.print("控制台模式[Console model]");
        Printer.instance.print("Character encoding with UTF-8");
        final Thread t = new Thread(() -> {
            Printer.instance.print("正在初始化服务器...");
            if (ConfigureReader.instance().getPropertiesStatus() == 0) {
                this.awaiting();
            }
            return;
        });
        t.start();
    }

    private void startServer() {
        Printer.instance.print("执行命令：启动服务器...");
        if (CtlRunner.ctl.started()) {
            Printer.instance.print("错误：服务器已经启动了。您可以使用 -status 命令查看服务器运行状态或使用 -stop 命令停止服务器。");
        } else if (CtlRunner.ctl.start()) {
            Printer.instance.print("服务已启动，可以正常访问了，您可以使用 -status 指令查看运行状态。");
        } else {
            if (ConfigureReader.instance().getPropertiesStatus() != 0) {
                switch (ConfigureReader.instance().getPropertiesStatus()) {
                    case BaseConfig.INVALID_PORT:
                        Printer.instance.print("错误：服务未能启动，端口设置无效。");
                        break;
                    case BaseConfig.INVALID_BUFFER_SIZE:
                        Printer.instance.print("错误：服务未能启动，缓存设置无效。");
                        break;
                    case BaseConfig.INVALID_FILE_SYSTEM_PATH:
                        Printer.instance.print("错误：服务未能启动，文件系统路径或某一扩展存储区设置无效。");
                        break;
                    case BaseConfig.INVALID_LOG:
                        Printer.instance.print("错误：服务未能启动，日志设置无效。");
                        break;
                    case BaseConfig.INVALID_VC:
                        Printer.instance.print("错误：服务未能启动，登录验证码设置无效。");
                        break;
                    default:
                        Printer.instance.print("错误：服务未能启动，请重试或检查设置。");
                        break;
                }
            } else {
                Printer.instance.print("错误：服务未能启动，请重试或检查设置。");
            }
        }
    }

    private void exit() {
        Printer.instance.print("执行命令：停止服务并退出...");
        if (CtlRunner.ctl.started() && CtlRunner.ctl.stop()) {
            Printer.instance.print("服务已关闭，停止所有访问。");
        }
        Printer.instance.print("退出应用。");
        System.exit(0);
    }

    private void restartServer() {
        Printer.instance.print("执行命令：重启服务器...");
        if (CtlRunner.ctl.started()) {
            if (CtlRunner.ctl.stop()) {
                if (CtlRunner.ctl.start()) {
                    Printer.instance.print("服务器重启成功，可以正常访问了。");
                } else {
                    Printer.instance.print("错误：无法重新启动服务器，请尝试手动启动。");
                }
            } else {
                Printer.instance.print("错误：无法关闭服务器，请尝试手动关闭。");
            }
        } else {
            Printer.instance.print("错误：服务器尚未启动。您可以使用 -start 命令启动服务器或使用 -status 命令查看服务器运行状态。");
        }
    }

    private void stopServer() {
        Printer.instance.print("执行命令：停止服务器...");
        if (CtlRunner.ctl.started()) {
            if (CtlRunner.ctl.stop()) {
                Printer.instance.print("服务器已关闭，停止所有访问。");
            } else {
                Printer.instance.print("错误：无法关闭服务器，您可以尝试强制关闭。");
            }
        } else {
            Printer.instance.print("错误：服务器尚未启动。您可以使用 -start 命令启动服务器或使用 -exit 命令退出应用。");
        }
    }

    private void awaiting() {
        Thread t = new Thread(() -> {
            reader = new Scanner(System.in);
            System.out.println("命令帮助：\r\n" + commandTips + "\r\n");
            try {
                while (true) {
                    System.out.print("command: console$ ");
                    String command = new String(reader.nextLine().getBytes("UTF-8"), "UTF-8");
                    switch (command) {
                        case C_START:
                        case DASH_C_START:
                            startServer();
                            break;
                        case C_STOP:
                        case DASH_C_STOP:
                            stopServer();
                            break;
                        case C_RESTART:
                        case DASH_C_RESTART:
                            restartServer();
                            break;
                        case C_STATUS:
                        case DASH_C_STATUS:
                            printServerStatus();
                            break;
                        case C_EXIT:
                        case DASH_C_EXIT:
                            reader.close();
                            exit();
                            return;
                        case C_HELP:
                        case DASH_C_HELP:
                        case DASH_LINE + DASH_C_HELP:
                            Printer.instance.print("命令帮助：\r\n" + commandTips);
                            break;
                        default:
                            Printer.instance.print("错误：无法识别的指令。\r\n" + commandTips);
                            break;
                    }
                }
            } catch (Exception e) {
                Printer.instance.print("错误：读取命令时出现意外导致程序退出，请重启服务。");
            }
        });
        t.start();
    }


    /**
     * 打印服务器状态
     */
    private void printServerStatus() {
        Printer.instance.print("服务器状态：\r\n<Port>端口号:" + ConfigureReader.instance().getPort() + "\r\n<LogLevel>日志等级:"
                + ConfigureReader.instance().getLogLevel() + "\r\n<BufferSize>缓冲区大小:"
                + ConfigureReader.instance().getBuffSize() + " B\r\n<FileSystemPath>文件系统存储路径："
                + ConfigureReader.instance().getAppPath() + "\r\n<MustLogin>是否必须登录："
                + ConfigureReader.instance().mustLogin() + "\r\n<Running>运行状态：" + CtlRunner.ctl.started());
    }

}
