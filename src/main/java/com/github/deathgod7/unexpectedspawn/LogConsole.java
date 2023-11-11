/*
 * This file is part of UnexpectedSpawn
 * (see https://github.com/DeathGOD7/unexpectedspawn-paper).
 *
 * Copyright (c) 2021 Shivelight.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.deathgod7.unexpectedspawn;

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
