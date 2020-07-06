package application.server.utils;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nacos
 */
public class SystemUtils {

    private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
        .getOperatingSystemMXBean();

    /**
     * nacos local ip
     */
    public static final String LOCAL_IP = InetUtils.getSelfIp();

    /**
     * The home of spring.application.name.
     */
    public static final String APP_ROOT = "app";

    /**
     * The key of application home.
     */
    public static final String APP_HOME_KEY = "deep.home";

    /**
     * The home of application.
     */
    public static final String APP_HOME = getAppHome();

    public static List<String> getIPsBySystemEnv(String key) {
        String env = getSystemEnv(key);
        List<String> ips = new ArrayList<>();
        if (StringUtils.isNotEmpty(env)) {
            ips = Arrays.asList(env.split(","));
        }
        return ips;
    }

    public static String getSystemEnv(String key) {
        return System.getenv(key);
    }

    public static float getLoad() {
        return (float) operatingSystemMXBean.getSystemLoadAverage();
    }

    public static float getCPU() {
        return (float) operatingSystemMXBean.getSystemCpuLoad();
    }

    public static float getMem() {
        return (float) (1 - (double) operatingSystemMXBean.getFreePhysicalMemorySize() / (double) operatingSystemMXBean
            .getTotalPhysicalMemorySize());
    }

    private static String getAppHome() {
        String appHome = System.getProperty(APP_HOME_KEY);
        if (StringUtils.isBlank(appHome)) {
            appHome = System.getProperty("user.home") + File.separator + "." + APP_ROOT;
        }
        return appHome;
    }
}
