package id.shivelight.paper.unexpectedspawn;

import org.bukkit.Bukkit;

public class LogConsole {
    static String logPrefix = "[UnexpectedSpawn] ";

    public static void severe(String msg) {
        Bukkit.getLogger().severe(logPrefix + msg);
    }
    public static void warn(String msg) {
        Bukkit.getLogger().warning(logPrefix + msg);
    }
    public static void info(String msg) {
        Bukkit.getLogger().info(logPrefix + msg);
    }
}
