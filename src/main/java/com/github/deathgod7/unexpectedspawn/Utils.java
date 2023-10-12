/*
 * This file is part of UnexpectedSpawn 
 * (see https://github.com/DeathGOD7/unexpectedspawn-paper).
 *
 * Copyright (c) 2023 DeathGOD7, Shivelight
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

import com.github.deathgod7.unexpectedspawn.UnexpectedSpawn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    static UnexpectedSpawn plugin = UnexpectedSpawn.getInstance();
    
    public enum ConfigVariable {
        XMin("x-min"),
        XMax("x-max"),
        ZMin("z-min"),
        ZMax("z-max"),
        FailRadius("fail-radius"),
        RespawnWorld("respawn-world"),
        BlackListMaterials("spawn-block-blacklist"),
        InvertBlock("invert-block-blacklist"),
        BlackListWorlds("blacklisted-worlds");
        
        final String configstring;
        ConfigVariable(String temp) {
            this.configstring = temp;
        }
    }
    public static String checkWorldConfig(World world, String config) {
        //List<String> worldList = plugin.config.getConfig().getStringList("worlds");
        String worldName = world.getName();
        if (plugin.config.getConfig().contains("worlds." + worldName + "." + config)) {
            return ("worlds." + worldName + ".");
        }
        else {
            return ("global.");
        }
    }
    
    public static int AddFailRange(int previous, int rangetoadd) {
        int result = 0;
        int valtype = Integer.signum(previous);
        
        if (valtype == 0 || valtype == 1){
            result = previous + rangetoadd;
        }
        else {
            result = previous - rangetoadd;
        }
        return result;
    }
    public static int getAreaValue(String value, ConfigVariable label, World world) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        else {
            String custom = checkWorldConfig(world, label.configstring);
            int temp = plugin.config.getConfig().getInt(custom + label.configstring);
            LogConsole.info("Used config: " + custom + " for "+ label.configstring + " (" + temp + ")", LogConsole.logTypes.debug);
            return temp;
        }
    }
    
    public static boolean getInvertStatus(ConfigVariable label, World world) {
        String custom = checkWorldConfig(world, label.configstring);
        boolean temp = plugin.config.getConfig().getBoolean(custom + label.configstring);
        LogConsole.info("Used config: " + custom + " and the blacklist invert is " + temp + " in world \"" + world.getName() + "\"", LogConsole.logTypes.debug);
        return temp;
    }
    public static HashSet<Material> getBlacklistedMaterials(ConfigVariable label, World world) {
        String custom = checkWorldConfig(world, label.configstring);
        HashSet<Material> blacklistedMaterial = new HashSet<>();
        List<String> materialList = plugin.config.getConfig().getStringList(custom + label.configstring);
        for (String name : materialList) {
            Material material = Material.getMaterial(name);
            if (material == null) {
                LogConsole.warn("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html", LogConsole.logTypes.log);
                continue;
            }
            blacklistedMaterial.add(material);
        }

        LogConsole.info("Used config: " + custom + " and the values are : " + blacklistedMaterial + " in world \"" + world.getName() + "\"", LogConsole.logTypes.debug);

        return blacklistedMaterial;
    }
    
    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Gets random spawn location based on config values from specified world
     * @param world World to get random location from
     * @return {@link Location}
     */
    public static Location getRandomSpawnLocation(World world) {
        int tryCount = 0;
        
        int xmin = getAreaValue(null, ConfigVariable.XMin, world);
        int xmax = getAreaValue(null, ConfigVariable.XMax, world);
        int zmin = getAreaValue(null, ConfigVariable.ZMin, world);
        int zmax = getAreaValue(null, ConfigVariable.ZMax, world);
        int retryonfail = getAreaValue(null, ConfigVariable.FailRadius, world);
        
        HashSet<Material> spawnBlacklistedMaterials = getBlacklistedMaterials(ConfigVariable.BlackListMaterials, world);
        
        boolean isSpawnBlacklistInverted = getInvertStatus(ConfigVariable.InvertBlock, world);
        
        while (true) {
            if(tryCount == 5000) {
                xmin = AddFailRange(xmin ,retryonfail);
                xmax = AddFailRange(xmax ,retryonfail);
                zmin = AddFailRange(zmin ,retryonfail);
                zmax = AddFailRange(zmax ,retryonfail);
                LogConsole.warn("Couldn't find suitable location after " + tryCount + " try. Updating range as per fail-radius.", LogConsole.logTypes.log);
                LogConsole.info("Updated area with retry fail radius ("+retryonfail+") so the current values are ("+xmin+","+xmax+","+zmin+","+zmax+").", LogConsole.logTypes.debug);
            }
            else if (tryCount >= 10000) {
                LogConsole.warn("Couldn't find suitable location for random respawn after "+tryCount+" so respawning at world spawn point.", LogConsole.logTypes.log);
                Location location = world.getSpawnLocation();
                return location.add(0.5d, 1d, 0.5d);
            }
            
            int x = xmin + ThreadLocalRandom.current().nextInt((xmax - xmin) + 1);
            int z = zmin + ThreadLocalRandom.current().nextInt((zmax - zmin) + 1);
            int y = world.getHighestBlockYAt(x, z);
            
            tryCount++;
            
            Location location = new Location(world, x, y, z);
            
            // Special case for server version < 1.15.2 (?)
            // Related: https://www.spigotmc.org/threads/gethighestblockat-returns-air.434090/
            if (location.getBlock().getType() == Material.AIR) {
                location = location.subtract(0, 1, 0);
            }
            
            if(!isSpawnBlacklistInverted) {
                if (spawnBlacklistedMaterials.contains(location.getBlock().getType())) {
                    continue;
                }
            }
            else {
                if (!spawnBlacklistedMaterials.contains(location.getBlock().getType())) {
                    continue;
                }
            }
            
            LogConsole.warn("Found location for random respawn after "+tryCount+" tries (X "+x+", Y "+y+", Z "+z+")", LogConsole.logTypes.log);
            
            return location.add(0.5d, 1d, 0.5d);
        }
    }

    /**
     * Gets random spawn location based on Xmin Xmax Zmin Zmax from specified world
     * @param xmin Minimum X value
     * @param xmax Maximum X value
     * @param zmin Minimum Z value
     * @param zmax Maximum Z value
     * @param world World to get random location from
     * @return {@link Location}
     */
    public static Location getRandomSpawnLocation(int xmin, int xmax, int zmin, int zmax, World world) {
        int tryCount = 0;

        int retryonfail = getAreaValue(null, ConfigVariable.FailRadius, world);

        HashSet<Material> spawnBlacklistedMaterials = getBlacklistedMaterials(ConfigVariable.BlackListMaterials, world);

        boolean isSpawnBlacklistInverted = getInvertStatus(ConfigVariable.InvertBlock, world);

        while (true) {
            if(tryCount == 5000) {
                xmin = AddFailRange(xmin ,retryonfail);
                xmax = AddFailRange(xmax ,retryonfail);
                zmin = AddFailRange(zmin ,retryonfail);
                zmax = AddFailRange(zmax ,retryonfail);
                LogConsole.warn("Couldn't find suitable location after " + tryCount + " try. Updating range as per fail-radius.", LogConsole.logTypes.log);
                LogConsole.info("Updated area with retry fail radius ("+retryonfail+") so the current values are ("+xmin+","+xmax+","+zmin+","+zmax+").", LogConsole.logTypes.debug);
            }
            else if (tryCount >= 10000) {
                LogConsole.warn("Couldn't find suitable location for random respawn after "+tryCount+" so respawning at world spawn point.", LogConsole.logTypes.log);
                Location location = world.getSpawnLocation();
                return location.add(0.5d, 1d, 0.5d);
            }

            int x = xmin + ThreadLocalRandom.current().nextInt((xmax - xmin) + 1);
            int z = zmin + ThreadLocalRandom.current().nextInt((zmax - zmin) + 1);
            int y = world.getHighestBlockYAt(x, z);

            tryCount++;

            Location location = new Location(world, x, y, z);

            // Special case for server version < 1.15.2 (?)
            // Related: https://www.spigotmc.org/threads/gethighestblockat-returns-air.434090/
            if (location.getBlock().getType() == Material.AIR) {
                location = location.subtract(0, 1, 0);
            }

            if(!isSpawnBlacklistInverted) {
                if (spawnBlacklistedMaterials.contains(location.getBlock().getType())) {
                    continue;
                }
            }
            else {
                if (!spawnBlacklistedMaterials.contains(location.getBlock().getType())) {
                    continue;
                }
            }

            LogConsole.warn("Found location for random respawn after "+tryCount+" tries (X "+x+", Y "+y+", Z "+z+")", LogConsole.logTypes.log);

            return location.add(0.5d, 1d, 0.5d);
        }
    }
    
    
    
    
}
