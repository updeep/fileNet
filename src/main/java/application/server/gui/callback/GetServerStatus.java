package application.server.gui.callback;

import application.server.pojo.enumeration.LogLevel;
import application.server.pojo.enumeration.VCLevel;

public interface GetServerStatus
{
    int getPropertiesStatus();
    
    boolean getServerStatus();
    
    int getPort();
    
    String getInitProt();
    
    int getBufferSize();
    
    String getInitBufferSize();
    
    LogLevel getLogLevel();
    
    LogLevel getInitLogLevel();
    
    VCLevel getVCLevel();
    
    VCLevel getInitVCLevel();
    
    String getAppPath();
    
    String getInitAppPath();

    String getWebAppPath();

    String getInitWebAppPath();
    
    boolean getMustLogin();

}
