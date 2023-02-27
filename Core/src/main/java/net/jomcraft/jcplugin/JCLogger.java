package net.jomcraft.jcplugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JCLogger {

    public static final Logger log = LogManager.getLogger("JCPlugin Launcher");
    public static final String version = JCLogger.class.getPackage().getImplementationVersion();

    public static boolean isEqualOrNewer(ComparableVersion defaultSettings) {
        final ComparableVersion jcplugin = new ComparableVersion(version);
        int diff = defaultSettings.compareTo(jcplugin);

        if(diff == 0 || diff > 0){
            return true;
        }
        return false;
    }

}