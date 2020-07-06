package application;

import application.server.boot.BaseCommand;
import application.server.boot.CtlRunner;
import application.server.boot.GuiRunner;

import java.util.Arrays;
import java.util.List;

/**
 * <h2>项目启动类</h2>
 * <p>该类为程序主类，内部的main方法为程序的唯一入口，负责接收命令传参并以不同模式启动。</p>
 *
 * @author devcp
 */
public class StartUp {
    /**
     * <h2>主方法</h2>
     *
     * @param args String[] 控制台传入参数
     *             Program arguments: -start -gui -console ...
     */
    public static void main(final String[] args) {

        // 默认启动方式
        String[] defs = {BaseCommand.DASH_C_START};
//        String[] defs = {BaseCommand.DASH_C_GUI};

        String[] target = args;
        if (target.length == 0) {
            target = defs;
        }
        List<String> argsList = Arrays.asList(target);
        if (argsList.contains(BaseCommand.DASH_C_GUI)) {
            startGui();
            return;
        }
        // 命令行模式启动
        CtlRunner.build(target);
    }

    /**
     * 图形用户界面模式启动
     */
    public static void startGui() {
        try {
            GuiRunner.build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(new String("错误！您的操作系统可能不支持图形界面启动，您可以在命令行加参数'-console'启动。".getBytes()));
        }
    }
}
