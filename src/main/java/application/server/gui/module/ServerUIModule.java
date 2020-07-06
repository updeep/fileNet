package application.server.gui.module;

import application.server.utils.reader.ConfigureReader;
import application.server.utils.reader.BaseConfig;
import application.server.gui.callback.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerUIModule extends BaseDynamicWindow {

    protected static JFrame window;
    private static SystemTray tray;
    private static TrayIcon trayIcon;
    private static JTextArea output;
    private static ServerUIModule instance;
    private static SettingWindow sw;
    private static OnCloseServer cs;
    private static OnStartServer ss;
    private static GetServerStatus st;
    private static GetServerTime ti;
    private static JButton start;
    private static JButton stop;
    private static JButton resatrt;
    private static JButton setting;
    private static JButton openBrowse;
    private static JButton exit;
    private static JLabel serverStatusLab;
    private static JLabel portStatusLab;
    private static JLabel logLevelLab;
    //    private static JLabel bufferSizeLab;
    private static final String S_STOP = "停止[Stopped]";
    private static final String S_START = "运行[Running]";
    private static final String S_STARTING = "启动中[Starting]...";
    private static final String S_STOPPING = "停止中[Stopping]...";
    protected static final String L_ALL = "记录全部(ALL)";
    protected static final String L_EXCEPTION = "仅异常(EXCEPTION)";
    protected static final String L_NONE = "不记录(NONE)";

    private ServerUIModule() {
        setUIFont();
        (ServerUIModule.window = new JFrame(L_HEADER_MAIN_TITLE)).setSize(OriginSize_Width, OriginSize_Height);
        ServerUIModule.window.setLocation(100, 100);
        ServerUIModule.window.setResizable(false);
        final String guiResFolder = ConfigureReader.instance().getAppPath() + BaseConfig.GUIS_DIR_NAME + File.separator;
        String iconImage = guiResFolder + "icon.png";
        String iconType = guiResFolder + "icon_tray.png";
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(iconImage));
            ServerUIModule.window.setIconImage(bufferedImage);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex2) {
            ex2.printStackTrace();
        }
        if (SystemTray.isSupported()) {
            ServerUIModule.window.setDefaultCloseOperation(1);
            ServerUIModule.tray = SystemTray.getSystemTray();
            if (System.getProperty("os.name").toLowerCase().indexOf("window") >= 0) {
                iconType = guiResFolder + "icon_tray_w.png";
            }
            try {
                (ServerUIModule.trayIcon = new TrayIcon(ImageIO.read(new File(iconType)))).setToolTip(L_TOOL_TIP);
                trayIcon.setImageAutoSize(true);
                final PopupMenu pMenu = new PopupMenu();
                final MenuItem exit = new MenuItem("退出(Exit)");
                final MenuItem show = new MenuItem("显示主窗口(Show)");
                trayIcon.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        // TODO 自动生成的方法存根

                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        // TODO 自动生成的方法存根

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        // TODO 自动生成的方法存根

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        // TODO 自动生成的方法存根

                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // TODO 自动生成的方法存根
                        if (e.getClickCount() == 2) {
                            show();
                        }
                    }
                });
                exit.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO 自动生成的方法存根
                        exit();
                    }
                });
                show.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO 自动生成的方法存根
                        show();
                    }
                });
                pMenu.add(exit);
                pMenu.addSeparator();
                pMenu.add(show);
                ServerUIModule.trayIcon.setPopupMenu(pMenu);
                ServerUIModule.tray.add(ServerUIModule.trayIcon);
            } catch (FileNotFoundException ex3) {
            } catch (IOException ex4) {
            } catch (AWTException ex5) {
            }
        } else {
            ServerUIModule.window.setDefaultCloseOperation(1);
        }
        ServerUIModule.window.setLayout(new BoxLayout(ServerUIModule.window.getContentPane(), 3));

        final JPanel titlebox = new JPanel(new FlowLayout(1));
        titlebox.setBorder(new EmptyBorder(0, 0, (int) (-25 * proportion), 0));
        final JLabel title = new JLabel(L_HEADER_MAIN_TITLE);
        title.setFont(new Font("宋体", 1, (int) (20 * proportion)));
        titlebox.add(title);
        ServerUIModule.window.add(titlebox);

        final JPanel subtitlebox = new JPanel(new FlowLayout(1));
        subtitlebox.setBorder(new EmptyBorder(0, 0, (int) (-20 * proportion), 0));
        final JLabel subtitle = new JLabel(L_HEADER_SUB_TITLE);
        subtitle.setFont(new Font("宋体", 0, (int) (13 * proportion)));
        subtitlebox.add(subtitle);
        ServerUIModule.window.add(subtitlebox);

        final JPanel statusBox = new JPanel(new GridLayout(3, 1));
        statusBox.setBorder(BorderFactory.createEtchedBorder());
        final JPanel serverStatus = new JPanel(new FlowLayout());
        serverStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        serverStatus.add(new JLabel("服务状态(Status)："));
        serverStatus.add(ServerUIModule.serverStatusLab = new JLabel("--"));
        statusBox.add(serverStatus);
        final JPanel portStatus = new JPanel(new FlowLayout());
        portStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        portStatus.add(new JLabel("端口号(Port)："));
        portStatus.add(ServerUIModule.portStatusLab = new JLabel("--"));
        statusBox.add(portStatus);
        final JPanel addrStatus = new JPanel(new FlowLayout());
        addrStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
        addrStatus.add(new JLabel("日志等级(LogLevel)："));
        addrStatus.add(ServerUIModule.logLevelLab = new JLabel("--"));
        statusBox.add(addrStatus);
//        final JPanel bufferStatus = new JPanel(new FlowLayout());
//        bufferStatus.setBorder(new EmptyBorder(0, 0, (int) (-8 * proportion), 0));
//        bufferStatus.add(new JLabel("下载缓冲区(Buffer)："));
//        bufferStatus.add(ServerUIModule.bufferSizeLab = new JLabel("--"));
//        statusBox.add(bufferStatus);
        ServerUIModule.window.add(statusBox);
        final JPanel buttonBox = new JPanel(new GridLayout(6, 1));
        buttonBox.add(ServerUIModule.start = new JButton("开启(Start)>>"));
        buttonBox.add(ServerUIModule.stop = new JButton("关闭(Stop)||"));
        buttonBox.add(ServerUIModule.resatrt = new JButton("重启(Restart)~>"));
        buttonBox.add(ServerUIModule.openBrowse = new JButton("浏览器(Browse)[*]"));
        buttonBox.add(ServerUIModule.setting = new JButton("设置(Setting)[/]"));
        buttonBox.add(ServerUIModule.exit = new JButton("退出(Exit)[X]"));
        ServerUIModule.window.add(buttonBox);

        final JPanel outputBox = new JPanel(new FlowLayout(1));
        outputBox.add(new JLabel("[输出信息(Server Message)]："));
        (ServerUIModule.output = new JTextArea()).setLineWrap(true);
        output.setRows(8 + (int) (proportion));
        output.setSize((int) (600 * proportion), 130);
        ServerUIModule.output.setEditable(false);
        ServerUIModule.output.setForeground(Color.RED);
        ServerUIModule.output.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根
                Thread t = new Thread(() -> {
                    if (output.getLineCount() >= 1000) {
                        int end = 0;
                        try {
                            end = output.getLineEndOffset(100);
                        } catch (Exception exc) {
                        }
                        output.replaceRange("", 0, end);
                    }
                    output.setCaretPosition(output.getText().length());
                });
                t.start();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // TODO 自动生成的方法存根
                output.selectAll();
                output.setCaretPosition(output.getSelectedText().length());
                output.requestFocus();
            }
        });
        outputBox.add(new JScrollPane(ServerUIModule.output));
        ServerUIModule.window.add(outputBox);
        final JPanel bottombox = new JPanel(new FlowLayout(1));
        bottombox.setBorder(new EmptyBorder(0, 0, (int) (-30 * proportion), 0));
        bottombox.add(new JLabel(L_FOOTER_TITLE));
        ServerUIModule.window.add(bottombox);
        ServerUIModule.start.setEnabled(false);
        ServerUIModule.stop.setEnabled(false);
        ServerUIModule.resatrt.setEnabled(false);
        ServerUIModule.openBrowse.setEnabled(false);
        ServerUIModule.setting.setEnabled(false);
        // 点击事件监听
        ServerUIModule.start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                start.setEnabled(false);
                setting.setEnabled(false);
                printMessage("启动服务...");
                if (ss != null) {
                    serverStatusLab.setText(S_STARTING);
                    Thread t = new Thread(() -> {
                        if (ss.start()) {
                            printMessage("启动完成。正在检查服务状态...");
                            if (st.getServerStatus()) {
                                printMessage("服务已经启动，可以正常访问了。");
                            } else {
                                printMessage("服务未能成功启动，请检查设置或查看异常信息。");
                            }
                        } else {
                            if (ConfigureReader.instance().getPropertiesStatus() != 0) {
                                switch (ConfigureReader.instance().getPropertiesStatus()) {
                                    case BaseConfig.INVALID_PORT:
                                        printMessage("服务无法启动：端口设置无效。");
                                        break;
                                    case BaseConfig.INVALID_BUFFER_SIZE:
                                        printMessage("服务无法启动：缓存设置无效。");
                                        break;
                                    case BaseConfig.INVALID_FILE_SYSTEM_PATH:
                                        printMessage("服务无法启动：文件系统路径或某一扩展存储区设置无效。");
                                        break;
                                    case BaseConfig.INVALID_LOG:
                                        printMessage("服务无法启动：日志设置无效。");
                                        break;
                                    case BaseConfig.INVALID_VC:
                                        printMessage("服务无法启动：登录验证码设置无效。");
                                        break;
                                    default:
                                        printMessage("服务无法启动，请检查设置或查看异常信息。");
                                        break;
                                }
                            } else {
                                printMessage("服务无法启动，请检查设置或查看异常信息。");
                            }
                            serverStatusLab.setText(S_STOP);
                        }
                        updateServerStatus();
                    });
                    t.start();
                }
            }
        });
        // 点击事件监听
        ServerUIModule.stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                stop.setEnabled(false);
                resatrt.setEnabled(false);
                openBrowse.setEnabled(false);
                printMessage("关闭服务...");
                Thread t = new Thread(() -> {
                    if (cs != null) {
                        serverStatusLab.setText(S_STOPPING);
                        if (cs.close()) {
                            printMessage("关闭完成。正在检查服务状态...");
                            if (st.getServerStatus()) {
                                printMessage("服务未能成功关闭，如有需要，可以强制关闭程序（不安全）。");
                            } else {
                                printMessage("服务已经关闭，停止所有访问。");
                            }
                        } else {
                            printMessage("服务无法关闭，请手动结束本程序。");
                        }
                        updateServerStatus();
                    }
                });
                t.start();
            }
        });
        // 点击事件监听
        ServerUIModule.exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                // openBrowse.setEnabled(false);
                exit();
            }
        });
        // 点击事件监听
        ServerUIModule.resatrt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                stop.setEnabled(false);
                resatrt.setEnabled(false);
                openBrowse.setEnabled(false);
                Thread t = new Thread(() -> {
                    printMessage("正在重启服务...");
                    if (cs.close()) {
                        if (ss.start()) {
                            printMessage("重启成功，可以正常访问了。");
                        } else {
                            printMessage("错误：服务已关闭但未能重新启动，请尝试手动启动服务。");
                        }
                    } else {
                        printMessage("错误：无法关闭服务，请尝试手动关闭。");
                    }
                    updateServerStatus();
                });
                t.start();
            }
        });
        // 点击事件监听
        ServerUIModule.openBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                try {
                    String url = OPEN_HOST + ":" + ServerUIModule.st.getPort();
                    java.net.URI uri = java.net.URI.create(url);
                    System.out.println(uri);
                    // 获取当前系统桌面扩展
                    java.awt.Desktop dp = java.awt.Desktop.getDesktop();

                    // 判断系统桌面是否支持要执行的功能
                    if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        dp.browse(uri);// 获取系统默认浏览器打开链接
                    }
                } catch (java.lang.NullPointerException e1) {
                    // 此为uri为空时抛出异常
                    e1.printStackTrace();
                } catch (java.io.IOException e2) {
                    // 此为无法获取系统默认浏览器
                    e2.printStackTrace();
                }
            }
        });
        // 点击事件监听
        ServerUIModule.setting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                ServerUIModule.sw = SettingWindow.getInstance();
                Thread t = new Thread(() -> {
                    sw.show();
                });
                t.start();
            }
        });
        modifyComponentSize(ServerUIModule.window);
    }

    public void show() {
        ServerUIModule.window.setVisible(true);
        updateServerStatus();
    }

    public static void setOnCloseServer(final OnCloseServer cs) {
        ServerUIModule.cs = cs;
    }

    public static ServerUIModule getInsatnce() {
        if (ServerUIModule.instance == null) {
            ServerUIModule.instance = new ServerUIModule();
        }
        return ServerUIModule.instance;
    }

    public static void setStartServer(final OnStartServer ss) {
        ServerUIModule.ss = ss;
    }

    public static void setGetServerStatus(final GetServerStatus st) {
        ServerUIModule.st = st;
        SettingWindow.st = st;
    }

    public void updateServerStatus() {
        if (ServerUIModule.st != null) {
            Thread t = new Thread(() -> {
                if (ServerUIModule.st.getServerStatus()) {
                    ServerUIModule.serverStatusLab.setText(S_START);
                    ServerUIModule.start.setEnabled(false);
                    ServerUIModule.stop.setEnabled(true);
                    ServerUIModule.resatrt.setEnabled(true);
                    ServerUIModule.setting.setEnabled(false);
                    ServerUIModule.openBrowse.setEnabled(true);
                } else {
                    ServerUIModule.serverStatusLab.setText(S_STOP);
                    ServerUIModule.start.setEnabled(true);
                    ServerUIModule.stop.setEnabled(false);
                    ServerUIModule.resatrt.setEnabled(false);
                    ServerUIModule.setting.setEnabled(true);
                    ServerUIModule.openBrowse.setEnabled(false);
                }
                ServerUIModule.portStatusLab.setText(ServerUIModule.st.getPort() + "");
                if (ServerUIModule.st.getLogLevel() != null) {
                    switch (ServerUIModule.st.getLogLevel()) {
                        case Event: {
                            ServerUIModule.logLevelLab.setText(L_ALL);
                            break;
                        }
                        case None: {
                            ServerUIModule.logLevelLab.setText(L_NONE);
                            break;
                        }
                        case Runtime_Exception: {
                            ServerUIModule.logLevelLab.setText(L_EXCEPTION);
                            break;
                        }
                        default: {
                            ServerUIModule.logLevelLab.setText("无法获取(?)");
                            break;
                        }
                    }
                }
//                ServerUIModule.bufferSizeLab.setText(ServerUIModule.st.getBufferSize() / 1024 + " KB");
            });
            t.start();
        }
    }

    private void exit() {
        ServerUIModule.start.setEnabled(false);
        ServerUIModule.stop.setEnabled(false);
        ServerUIModule.exit.setEnabled(false);
        ServerUIModule.resatrt.setEnabled(false);
        ServerUIModule.setting.setEnabled(false);
        ServerUIModule.openBrowse.setEnabled(false);
        this.printMessage("退出程序...");
        if (ServerUIModule.cs != null) {
            final Thread t = new Thread(() -> {
                if (ServerUIModule.st.getServerStatus()) {
                    ServerUIModule.cs.close();
                }
                System.exit(0);
                return;
            });
            t.start();
        } else {
            System.exit(0);
        }
    }

    public void printMessage(final String context) {
        ServerUIModule.output.append("[" + this.getFormateDate() + "]" + context + "\n");
    }

    private String getFormateDate() {
        if (ServerUIModule.ti != null) {
            final Date d = ServerUIModule.ti.get();
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(d);
        }
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    public static void setGetServerTime(final GetServerTime ti) {
        ServerUIModule.ti = ti;
    }

    public static void setUpdateSetting(final UpdateSetting us) {
        SettingWindow.us = us;
    }
}
