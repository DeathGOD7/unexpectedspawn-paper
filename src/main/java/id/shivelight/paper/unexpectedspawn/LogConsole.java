package id.shivelight.paper.unexpectedspawn;

import org.bukkit.Bukkit;

public class LogConsole {

    static String logPrefix = "[UnexpectedSpawn] ";
    enum logTypes {
        log,
        debug
    }
    public static Boolean debugMode = UnexpectedSpawn.getPlugin(UnexpectedSpawn.class).getConfig().getBoolean("debug");

    public static void severe(String msg, logTypes logType) {
        if (logType == logTypes.debug) {
            if (debugMode) { Bukkit.getLogger().severe(logPrefix + msg); }
        }
        else if (logType == logTypes.log) {
            Bukkit.getLogger().severe(logPrefix + msg);
        }
    }

    public static void warn(String msg, logTypes logType) {
        if (logType == logTypes.debug) {
            if (debugMode) { Bukkit.getLogger().warning(logPrefix + msg); }
        }
        else if (logType == logTypes.log) {
            Bukkit.getLogger().warning(logPrefix + msg);
        }
    }

    public static void info(String msg, logTypes logType) {
        if (logType == logTypes.debug) {
            if (debugMode) { Bukkit.getLogger().info(logPrefix + msg); }
        }
        else if (logType == logTypes.log) {
            Bukkit.getLogger().info(logPrefix + msg);
        }
    }

}
