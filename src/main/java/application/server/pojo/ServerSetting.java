package application.server.pojo;

import application.server.pojo.enumeration.LogLevel;
import application.server.pojo.enumeration.VCLevel;

public class ServerSetting {
	private boolean mustLogin;
	private VCLevel vc;
	private int buffSize;
	private LogLevel log;
	private int port;
	private String fsPath;

	public boolean isMustLogin() {
		return this.mustLogin;
	}

	public void setMustLogin(final boolean mustLogin) {
		this.mustLogin = mustLogin;
	}

	public int getBuffSize() {
		return this.buffSize;
	}

	public void setBuffSize(final int buffSize) {
		this.buffSize = buffSize;
	}

	public LogLevel getLog() {
		return this.log;
	}

	public void setLog(final LogLevel log) {
		this.log = log;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public String getFsPath() {
		return this.fsPath;
	}

	public void setFsPath(final String fsPath) {
		this.fsPath = fsPath;
	}

	public VCLevel getVc() {
		return this.vc;
	}

	public void setVc(VCLevel vc) {
		this.vc = vc;
	}
}
