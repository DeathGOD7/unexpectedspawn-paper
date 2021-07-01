/*
 * This file is part of UnexpectedSpawn
 * (see https://github.com/Shivelight/unexpectedspawn-paper).
 *
 * Copyright (c) 2019 Shivelight.
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Spawn implements Listener {

    private final UnexpectedSpawn plugin;
    private final HashSet<Material> blacklistedMaterial = new HashSet<>();
    private final HashSet<World> blackListedWorlds = new HashSet<>();

    Spawn(UnexpectedSpawn plugin) {
        this.plugin = plugin;

//        List<String> materialList = plugin.config.getConfig().getStringList("global.spawn-block-blacklist");
//        for (String name : materialList) {
//            Material material = Material.getMaterial(name);
//            if (material == null) {
//                Bukkit.getLogger().warning("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html");
//                continue;
//            }
//            blacklistedMaterial.add(material);
//        }

        List<String> worldList = plugin.config.getConfig().getStringList("blacklisted-worlds");
        for (String name: worldList) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                logMessageConsole("Couldn't find world "+ name + ". Either it doesn't exist or is not valid.","warn");
                continue;
            }
            blackListedWorlds.add(world);
        }
    }

    private void logMessageConsole(String msg, String type) {
        String logPrefix = "[UnexpectedSpawn] ";
        if (type.equals("severe")) {
            Bukkit.getLogger().severe(logPrefix + msg);
        }
        else if (type.equals("warn")) {
            Bukkit.getLogger().warning(logPrefix + msg);
        }
        else {
            Bukkit.getLogger().info(logPrefix + msg);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World joinWorld = event.getPlayer().getWorld();

        if (joinWorld.getEnvironment().equals(World.Environment.NETHER)
                || joinWorld.getEnvironment().equals(World.Environment.THE_END)) {
            return;
        }

        if (blackListedWorlds.contains(joinWorld)) {
            logMessageConsole("User in blacklisted world. So random join spawn is disabled.","info");
            return;
        }

        String useCustomOnFirstJoin = checkWorldConfig(joinWorld, "random-respawn.on-first-join");
        String useCustomAlwaysOnJoin = checkWorldConfig(joinWorld, "random-respawn.always-on-join");

        logMessageConsole("Used config: " + useCustomOnFirstJoin + " for first join and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"), "info");
        logMessageConsole("Used config: " + useCustomAlwaysOnJoin + " for always on join and the values is "+ plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join"), "info");

        if ((!event.getPlayer().hasPlayedBefore() && plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"))
                || plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join")) {
            Location joinLocation = getRandomSpawnLocation(joinWorld);
            event.getPlayer().teleport(joinLocation);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // List<World> allWorlds = Bukkit.getWorlds();
        World respawnWorld = event.getRespawnLocation().getWorld();

        if (blackListedWorlds.contains(respawnWorld)) {
            logMessageConsole("User in blacklisted world. So random respawn is disabled.","info");
            return;
        }

        if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
            return;
        }

        String useCustomOnDeath = checkWorldConfig(respawnWorld, "random-respawn.on-death");
        String useCustomBedRespawn = checkWorldConfig(respawnWorld, "random-respawn.bed-respawn-enabled");

        logMessageConsole("Used config: " + useCustomOnDeath + " for on death and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death"), "info");
        logMessageConsole("Used config: " + useCustomBedRespawn + " for bed respawn and the values is "+ plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled"), "info");

        if(plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death")) {
            if (!event.isBedSpawn() || !plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled")) {
                Location respawnLocation = getRandomSpawnLocation(respawnWorld);
                event.setRespawnLocation(respawnLocation);
            }
        }
    }


    private String checkWorldConfig(World world, String config) {
        List<String> worldList = plugin.config.getConfig().getStringList("worlds");
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
                logMessageConsole("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html", "warn");
                continue;
            }
            blacklistedMaterial.add(material);
        }
        return blacklistedMaterial;
    }

    private Location getRandomSpawnLocation(World world) {
        String useCustomMinX = checkWorldConfig(world, "x-min");
        String useCustomMaxX = checkWorldConfig(world, "x-max");
        String useCustomMinZ = checkWorldConfig(world, "z-min");
        String useCustomMaxZ = checkWorldConfig(world, "z-max");

        int xmin = plugin.config.getConfig().getInt(useCustomMinX + "x-min");
        int xmax = plugin.config.getConfig().getInt(useCustomMaxX + "x-max");
        int zmin = plugin.config.getConfig().getInt(useCustomMinZ + "z-min");
        int zmax = plugin.config.getConfig().getInt(useCustomMaxZ + "z-max");

        logMessageConsole("Used config: " + useCustomMinX + " for random respawn area and the values are ("+xmin+","+xmax+","+zmin+","+zmax+")","info");

        String useCustomBlacklistedMaterials = checkWorldConfig(world, "spawn-block-blacklist");
        HashSet<Material> blacklistedMaterials = getBlacklistedMaterials(useCustomBlacklistedMaterials);

        logMessageConsole("Used config: " + useCustomBlacklistedMaterials + " and the values are : " + blacklistedMaterials,"info");

        while (true) {
            int x = xmin + ThreadLocalRandom.current().nextInt((xmax - xmin) + 1);
            int z = zmin + ThreadLocalRandom.current().nextInt((zmax - zmin) + 1);
            int y = world.getHighestBlockYAt(x, z);

            Location location = new Location(world, x, y, z);

            // Special case for server version <1.15.2 (?)
            // Related: https://www.spigotmc.org/threads/gethighestblockat-returns-air.434090/
            if (location.getBlock().getType() == Material.AIR) {
                location = location.subtract(0, 1, 0);
            }

            if (blacklistedMaterials.contains(location.getBlock().getType())) {
                continue;
            }

            return location.add(0.5d, 1d, 0.5d);
        }
    }
}
