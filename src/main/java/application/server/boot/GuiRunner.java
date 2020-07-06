package application.server.boot;

import application.server.utils.reader.ConfigureReader;
import application.server.pojo.enumeration.LogLevel;
import application.server.pojo.enumeration.VCLevel;
import application.server.gui.callback.GetServerStatus;
import application.server.gui.callback.UpdateSetting;
import application.server.gui.module.ServerUIModule;
import application.server.gui.printer.Printer;
import application.server.pojo.ServerSetting;
import application.server.utils.ServerTimeUtil;

/**
 * 
 * <h2>GUI界面模式启动器</h2>
 * <p>
 * 该启动器将以界面模式启动，请执行静态build()方法开启界面并初始化服务配置。
 * </p>
 * 
 * @author devcp
 * @version 1.0
 */
public class GuiRunner {

	private static GuiRunner gui;

	private GuiRunner() {
		Printer.init(true);
		final ServerUIModule ui = ServerUIModule.getInsatnce();
		// 服务器控制层，用于连接GUI与服务器内核
		AppCtl ctl = new AppCtl();
		ServerUIModule.setStartServer(() -> ctl.start());
		ServerUIModule.setOnCloseServer(() -> ctl.stop());
		ServerUIModule.setGetServerTime(() -> ServerTimeUtil.getServerTime());
		ServerUIModule.setGetServerStatus(new GetServerStatus() {

			@Override
			public boolean getServerStatus() {
				// TODO 自动生成的方法存根
				return ctl.started();
			}

			@Override
			public int getPropertiesStatus() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getPropertiesStatus();
			}

			@Override
			public int getPort() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getPort();
			}

			@Override
			public boolean getMustLogin() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().mustLogin();
			}

			@Override
			public LogLevel getLogLevel() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getLogLevel();
			}

			@Override
			public int getBufferSize() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getBuffSize();
			}

			@Override
			public VCLevel getVCLevel() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getVCLevel();
			}


			@Override
			public LogLevel getInitLogLevel() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitLogLevel();
			}

			@Override
			public VCLevel getInitVCLevel() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitVCLevel();
			}

			@Override
			public String getAppPath() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getAppPath();
			}

			@Override
			public String getInitAppPath() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitAppPath();
			}

			@Override
			public String getWebAppPath() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getWebAppPath();
			}

			@Override
			public String getInitWebAppPath() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitWebAppPath();
			}

			@Override
			public String getInitProt() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitPort();
			}

			@Override
			public String getInitBufferSize() {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().getInitBuffSize();
			}
		});
		ServerUIModule.setUpdateSetting(new UpdateSetting() {

			@Override
			public boolean update(ServerSetting s) {
				// TODO 自动生成的方法存根
				return ConfigureReader.instance().doUpdate(s);
			}
		});
		ui.show();
	}

	/**
	 * 
	 * <h2>GUI模式运行</h2>
	 * <p>
	 * 启动GUI模式操作并初始化服务器引擎，该方法将返回本启动器的唯一实例。
	 * </p>
	 *
	 * @return application.handler.GuiRunner 本启动器唯一实例
	 */
	public static GuiRunner build() {
		if (GuiRunner.gui == null) {
			GuiRunner.gui = new GuiRunner();
		}
		return GuiRunner.gui;
	}
}
