/*
 * This file is part of UnexpectedSpawn
 * (see https://github.com/Shivelight/unexpectedspawn-paper).
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

package id.shivelight.paper.unexpectedspawn;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Spawn implements Listener {

    private final UnexpectedSpawn plugin;
    private final HashSet<Material> blacklistedMaterial = new HashSet<>();
    private final HashSet<World> blacklistedWorlds = new HashSet<>();

    Spawn(UnexpectedSpawn plugin) {
        this.plugin = plugin;

        List<String> worldList = plugin.config.getConfig().getStringList("blacklisted-worlds");
        for (String name : worldList) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                LogConsole.warn("Couldn't find world " + name + ". Either it doesn't exist or is not valid.", LogConsole.logTypes.log);
                continue;
            }
            blacklistedWorlds.add(world);
        }
    }

    World deathWorld;
    Player deadPlayer;
    Location deathLocation;


    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        deathWorld = event.getEntity().getWorld();
        deadPlayer = event.getEntity();
        deathLocation = deadPlayer.getLocation();
        LogConsole.info("Player " + deadPlayer.getName() + " died at (X "
                    + deathLocation.getBlockX() + ", Y " + deathLocation.getBlockY() + ", Z " + deathLocation.getBlockZ() +
                    ") at world (" + deathWorld.getName() + ").", LogConsole.logTypes.debug);

        if (deadPlayer != null && deadPlayer.hasPermission("unexpectedspawn.notify")) {
            String msg = String.format("Your death location (&4X %s&r, &2Y %s&r, &1Z %s&r) in world (%s).", deathLocation.getBlockX(), deathLocation.getBlockY(), deathLocation.getBlockZ(), deathWorld.getName());
            String out = ChatColor.translateAlternateColorCodes('&', msg);
            deadPlayer.sendMessage(out);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World joinWorld = event.getPlayer().getWorld();

        if (event.getPlayer().hasPermission("unexpectedspawn.bypass")) {
            LogConsole.info("Player " + event.getPlayer().getName()
                + " has (unexpectedspawn.bypass) permission. So skipping random join spawn.", LogConsole.logTypes.debug);
            return;
        }

        if (joinWorld.getEnvironment().equals(World.Environment.NETHER)
                || joinWorld.getEnvironment().equals(World.Environment.THE_END)) {
            LogConsole.info("User in NETHER or END. So random join spawn is disabled.", LogConsole.logTypes.debug);
            return;
        }

        if (blacklistedWorlds.contains(joinWorld)) {
            LogConsole.info("User in blacklisted world (" + joinWorld + "). So random join spawn is disabled.", LogConsole.logTypes.debug);
            return;
        }

        String useCustomOnFirstJoin = checkWorldConfig(joinWorld, "random-respawn.on-first-join");
        String useCustomAlwaysOnJoin = checkWorldConfig(joinWorld, "random-respawn.always-on-join");
        
        if ((!event.getPlayer().hasPlayedBefore() && plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"))
                || plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join")) {
            Location joinLocation = getRandomSpawnLocation(joinWorld);
            event.getPlayer().teleport(joinLocation);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        World respawnWorld = event.getRespawnLocation().getWorld();
        String wName = respawnWorld.getName();

        if (event.getPlayer().hasPermission("unexpectedspawn.bypass")) {
            LogConsole.info("Player " + event.getPlayer().getName()
                    + " has (unexpectedspawn.bypass) permission. So skipping random respawn.", LogConsole.logTypes.debug);
            return;
        }

        if (deathWorld != null) {
            String useCustomWorld = checkWorldConfig(deathWorld, "respawn-world");
            String obtainedData = plugin.config.getConfig().getString(useCustomWorld + "respawn-world");

            if (obtainedData != null) {
                if (obtainedData.isEmpty()) {
                    respawnWorld = deathWorld;
                    wName = respawnWorld.getName();
                    LogConsole.info("Using world (" + wName + ") where player died.", LogConsole.logTypes.debug);
                }

                else {
                    World obtainedWorld = Bukkit.getWorld(obtainedData);

                    if (obtainedWorld == null) {
                        LogConsole.warn("Couldn't find world " + obtainedData + ". Either it doesn't exist or is not valid.", LogConsole.logTypes.log);
                        respawnWorld = deathWorld;
                        wName = respawnWorld.getName();
                        LogConsole.info("Using world (" + wName + ") where player died.", LogConsole.logTypes.debug);
                    }

                    else {
                        respawnWorld = obtainedWorld;

                        wName = respawnWorld.getName();
                        LogConsole.info("Using world (" + wName + ") specified in config.", LogConsole.logTypes.debug);
                    }
                }
            } else {
                LogConsole.severe("Respawn World in (" + useCustomWorld + ") cannot be null. Please add empty string to disable it.", LogConsole.logTypes.log);
            }
        }
        else {
            LogConsole.info("Using world (" + wName + ") where player will respawn normally. Probably coming back to OVERWORLD from END.", LogConsole.logTypes.debug);
        }

        if (blacklistedWorlds.contains(respawnWorld)) {
            LogConsole.info("User in blacklisted world ("+ respawnWorld +"). So random respawn is disabled.", LogConsole.logTypes.debug);
            return;
        }

        if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
            return;
        }

        String useCustomOnDeath = checkWorldConfig(respawnWorld, "random-respawn.on-death");
        String useCustomBedRespawn = checkWorldConfig(respawnWorld, "random-respawn.bed-respawn-enabled");


        if(plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death")) {
            if (!event.isBedSpawn() || !plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled")) {
                Location respawnLocation = getRandomSpawnLocation(respawnWorld);
                event.setRespawnLocation(respawnLocation);
            }
        }
    }


    private String checkWorldConfig(World world, String config) {
        //List<String> worldList = plugin.config.getConfig().getStringList("worlds");
        String worldName = world.getName();
        if (plugin.config.getConfig().contains("worlds." + worldName + "." + config)) {
            return ("worlds." + worldName + ".");
        }
        else {
            return ("global.");
        }
    }

    private HashSet<Material> getBlacklistedMaterials(String prefix) {
        List<String> materialList = plugin.config.getConfig().getStringList(prefix + "spawn-block-blacklist");
        blacklistedMaterial.clear();
        for (String name : materialList) {
            Material material = Material.getMaterial(name);
            if (material == null) {
                LogConsole.warn("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html", LogConsole.logTypes.log);
                continue;
            }
            blacklistedMaterial.add(material);
        }
        return blacklistedMaterial;
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

    private Location getRandomSpawnLocation(World world) {
        int tryCount = 0;
        String useCustomMinX = checkWorldConfig(world, "x-min");
        String useCustomMaxX = checkWorldConfig(world, "x-max");
        String useCustomMinZ = checkWorldConfig(world, "z-min");
        String useCustomMaxZ = checkWorldConfig(world, "z-max");
        String useCustomRetryOnFail = checkWorldConfig(world, "fail-radius");

        int xmin = plugin.config.getConfig().getInt(useCustomMinX + "x-min");
        int xmax = plugin.config.getConfig().getInt(useCustomMaxX + "x-max");
        int zmin = plugin.config.getConfig().getInt(useCustomMinZ + "z-min");
        int zmax = plugin.config.getConfig().getInt(useCustomMaxZ + "z-max");
        int retryonfail = plugin.config.getConfig().getInt(useCustomRetryOnFail + "fail-radius");


        LogConsole.info("Used config: " + useCustomMinX + " for random respawn area and the values are ("+xmin+","+xmax+","+zmin+","+zmax+").", LogConsole.logTypes.debug);


        String useCustomBlacklistedMaterials = checkWorldConfig(world, "spawn-block-blacklist");
        HashSet<Material> worldBlacklistedMaterials = getBlacklistedMaterials(useCustomBlacklistedMaterials);

        String useCustomSpawnBlacklistInverted = checkWorldConfig(world, "invert-block-blacklist");
        boolean isSpawnBlacklistInverted = plugin.config.getConfig().getBoolean(useCustomSpawnBlacklistInverted + "invert-block-blacklist");

        LogConsole.info("Used config: " + useCustomSpawnBlacklistInverted + " and the blacklist invert is " + isSpawnBlacklistInverted + " in " + world.getName(), LogConsole.logTypes.debug);
        LogConsole.info("Used config: " + useCustomBlacklistedMaterials + " and the values are : " + worldBlacklistedMaterials, LogConsole.logTypes.debug);

        while (true) {

            if(tryCount == 5000) {
                xmin = AddFailRange(xmin ,retryonfail);
                xmax = AddFailRange(xmax ,retryonfail);
                zmin = AddFailRange(zmin ,retryonfail);
                zmax = AddFailRange(zmax ,retryonfail);
                LogConsole.warn("Couldn't find suitable location after "+tryCount+" try. Updating range as per fail-radius.", LogConsole.logTypes.log);
                LogConsole.info("Used config: " + useCustomRetryOnFail + " for retry fail radius ("+retryonfail+") so the current values are ("+xmin+","+xmax+","+zmin+","+zmax+").", LogConsole.logTypes.debug);
            }
            else if (tryCount >= 10000) {
                LogConsole.warn("Couldn't find suitable location for random respawn after "+tryCount+" so respawning at world spawn point.", LogConsole.logTypes.log);
                Location location = world.getSpawnLocation();
                //Location location = new Location(world,0,world.getHighestBlockYAt(0, 0),0);
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
                if (worldBlacklistedMaterials.contains(location.getBlock().getType())) {
                 continue;
                }
            }
            else {
                if (!worldBlacklistedMaterials.contains(location.getBlock().getType())) {
                    continue;
                }
            }

            LogConsole.warn("Found location for random respawn after "+tryCount+" (X "+x+", Y "+y+", Z "+z+")", LogConsole.logTypes.log);

            return location.add(0.5d, 1d, 0.5d);
        }
    }
}
