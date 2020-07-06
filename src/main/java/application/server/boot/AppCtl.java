package application.server.boot;

import application.config.AppConfig;
import application.server.gui.printer.Printer;
import application.server.utils.SystemUtils;
import application.server.utils.reader.ConfigureReader;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

/**
 * <h2>服务引擎控制器</h2>
 * <p>
 * 该类为服务引擎的控制层，负责连接服务内核与用户操作界面，用于控制服务行为。包括启动、关闭、重启等。同时，该类也为SpringBoot框架
 * 应用入口，负责初始化SpringBoot容器。详见内置公有方法。
 * </p>
 *
 * @author devcp
 * @version 1.0
 */
@Import({AppConfig.class})
public class AppCtl {
    private static final String OPEN_HOST = "http://" + SystemUtils.LOCAL_IP;
    private static ApplicationContext context;
    private static boolean run;

    /**
     * <h2>启动服务</h2>
     * <p>
     * 该方法将启动SpringBoot服务引擎并返回启动结果。该过程较为耗时，为了不阻塞主线程，请在额外线程中执行该方法。
     * </p>
     *
     * @return boolean 启动结果
     * @author devcp
     */
    public boolean start() {
        Printer.instance.print("正在初始化服务设置...");
        final String[] args = new String[0];
        if (!AppCtl.run) {
            ConfigureReader cr = ConfigureReader.instance();
            //启动服务前重新检查各项设置并加载
            cr.checkServerPropertiesAndEffectRe();
            if (cr.getPropertiesStatus() == 0) {
                try {
                    Printer.instance.print("正在开启服务引擎...");
                    AppCtl.context = SpringApplication.run(AppCtl.class, args);
                    AppCtl.run = (AppCtl.context != null);
                    Printer.instance.print("服务已启动成功");
                    Printer.instance.print("Console: " + OPEN_HOST + ":" + cr.getPort() + "/index.html");
                    return AppCtl.run;
                } catch (Exception e) {
                    return false;
                }
            }
            Printer.instance.print("服务设置检查失败，无法开启服务。");
            return false;
        }
        Printer.instance.print("服务正在运行中。");
        return true;
    }

    /**
     * <h2>停止服务</h2>
     * <p>
     * 该方法将关闭服务引擎并清理缓存文件。该方法较为耗时。
     * </p>
     *
     * @return boolean 关闭结果
     * @author devcp
     */
    public boolean stop() {
        Printer.instance.print("正在关闭服务...");
        if (AppCtl.context != null) {
            Printer.instance.print("正在终止服务引擎...");
            try {
                AppCtl.run = (SpringApplication.exit(AppCtl.context, new ExitCodeGenerator[0]) != 0);
                Printer.instance.print("服务引擎已终止。");
                return !AppCtl.run;
            } catch (Exception e) {
                return false;
            }
        }
        Printer.instance.print("服务未启动。");
        return true;
    }

    /**
     * <h2>获取服务运行状态</h2>
     * <p>
     * 该方法返回服务引擎的运行状态，该状态由内置属性记录，且唯一。
     * </p>
     *
     * @return boolean 服务是否启动
     * @author devcp
     */
    public boolean started() {
        return AppCtl.run;
    }

    static {
        AppCtl.run = false;
    }

    /**
     * SpringBoot内置Tomcat引擎必要设置：端口、错误页面及HTTPS支持
     *
     * @return
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        // 创建Tomcat容器引擎，分为开启https和不开启https两种模式
        TomcatServletWebServerFactory tomcat = null;
        if (ConfigureReader.instance().openHttps()) {
            // 对于开启https模式，则将http端口的请求全部转发至https端口处理
            tomcat = new TomcatServletWebServerFactory() {
                // 设置默认http处理转发
                @Override
                protected void customizeConnector(Connector connector) {
                    connector.setScheme("http");
                    // Connector监听的http的端口号
                    connector.setPort(ConfigureReader.instance().getPort());
                    connector.setSecure(false);
                    // 监听到http的端口号后转向到的https的端口号
                    connector.setRedirectPort(ConfigureReader.instance().getHttpsPort());
                }

                // 设置默认http处理
                @Override
                protected void postProcessContext(Context context) {
                    SecurityConstraint constraint = new SecurityConstraint();
                    constraint.setUserConstraint("CONFIDENTIAL");
                    SecurityCollection collection = new SecurityCollection();
                    collection.addPattern("/*");
                    constraint.addCollection(collection);
                    context.addConstraint(constraint);
                }
            };
            // 添加https链接处理器
            tomcat.addAdditionalTomcatConnectors(createHttpsConnector());
        } else {
            // 对于不开启https模式，以常规方法生成容器
            tomcat = new TomcatServletWebServerFactory();
            tomcat.setPort(ConfigureReader.instance().getPort());
        }
        // 设置错误处理页面
        tomcat.addErrorPages(new ErrorPage[]{new ErrorPage(HttpStatus.NOT_FOUND, "/errorController/pageNotFound.do"),
                new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errorController/pageNotFound.do")});
        return tomcat;
    }

    /**
     * 生成https支持配置，包括端口号、证书文件、证书密码等
     *
     * @return
     */
    private Connector createHttpsConnector() {
        // 配置针对Https的支持
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        // 设置请求协议头
        connector.setScheme("https");
        // 设置https请求端口
        connector.setPort(ConfigureReader.instance().getHttpsPort());
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        // 开启SSL加密通信
        protocol.setSSLEnabled(true);
        // 设置证书文件
        protocol.setKeystoreFile(ConfigureReader.instance().getHttpsKeyFile());
        // 设置加密类别（PKCS12/JKS）
        protocol.setKeystoreType(ConfigureReader.instance().getHttpsKeyType());
        // 设置证书密码
        protocol.setKeystorePass(ConfigureReader.instance().getHttpsKeyPass());
        return connector;
    }
}
