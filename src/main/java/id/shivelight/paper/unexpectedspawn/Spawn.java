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
        for (String name : worldList) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                LogConsole.warn("Couldn't find world " + name + ". Either it doesn't exist or is not valid.");
                continue;
            }
            blacklistedWorlds.add(world);
        }
    }

    World deathWorld;

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        deathWorld = event.getEntity().getWorld();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World joinWorld = event.getPlayer().getWorld();

        if (joinWorld.getEnvironment().equals(World.Environment.NETHER)
                || joinWorld.getEnvironment().equals(World.Environment.THE_END)) {
            return;
        }

        if (blacklistedWorlds.contains(joinWorld)) {
            // Debugger Start
            //LogConsole.info("User in blacklisted world["+ joinWorld +"]. So random join spawn is disabled.");
            // Debugger End
            return;
        }

        String useCustomOnFirstJoin = checkWorldConfig(joinWorld, "random-respawn.on-first-join");
        String useCustomAlwaysOnJoin = checkWorldConfig(joinWorld, "random-respawn.always-on-join");

        // Debugger Start
        //LogConsole.info("Used config: " + useCustomOnFirstJoin + " for first join and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"));
        //LogConsole.info("Used config: " + useCustomAlwaysOnJoin + " for always on join and the values is "+ plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join"));
        // Debugger End
        
        if ((!event.getPlayer().hasPlayedBefore() && plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"))
                || plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join")) {
            Location joinLocation = getRandomSpawnLocation(joinWorld);
            event.getPlayer().teleport(joinLocation);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        World respawnWorld = event.getRespawnLocation().getWorld();

        String useCustomWorld = checkWorldConfig(deathWorld, "respawn-world");
        String obtainedData = plugin.config.getConfig().getString(useCustomWorld + "respawn-world");

        if (obtainedData != null) {
            if(obtainedData.isEmpty()){
                respawnWorld = deathWorld;
                String wName = respawnWorld.getName();
                LogConsole.info("Using world ("+ wName +") where player died.");
            }
            else {
                World obtainedWorld = Bukkit.getWorld(obtainedData);
                if (obtainedWorld == null) {
                    LogConsole.warn("Couldn't find world "+ obtainedData + ". Either it doesn't exist or is not valid.");
                    respawnWorld = deathWorld;;
                    String wName = respawnWorld.getName();
                    LogConsole.info("Using world ("+ wName +") where player died.");
                }
                else {
                    respawnWorld = obtainedWorld;

                    String wName = respawnWorld.getName();
                    LogConsole.info("Using world ("+ wName +") specified in config.");
                }
            }
        }
        else {
            LogConsole.severe("Respawn World in ("+useCustomWorld+") cannot be null. Please add empty string to disable it.");
        }

        if (blacklistedWorlds.contains(respawnWorld)) {
            // Debugger Start
            //LogConsole.info("User in blacklisted world["+ respawnWorld +"]. So random respawn is disabled.","info");
            // Debugger End
            return;
        }

        if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
            return;
        }

        String useCustomOnDeath = checkWorldConfig(respawnWorld, "random-respawn.on-death");
        String useCustomBedRespawn = checkWorldConfig(respawnWorld, "random-respawn.bed-respawn-enabled");

        // Debugger Start
        //LogConsole.info("Used config: " + useCustomOnDeath + " for on death and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death"), "info");
        //LogConsole.info("Used config: " + useCustomBedRespawn + " for bed respawn and the values is "+ plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled"), "info");
        // Debugger End

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
                LogConsole.warn("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html");
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

        // Debugger Start
        //LogConsole.info("Used config: " + useCustomMinX + " for random respawn area and the values are ("+xmin+","+xmax+","+zmin+","+zmax+")","info");
        // Debugger End

        String useCustomBlacklistedMaterials = checkWorldConfig(world, "spawn-block-blacklist");
        HashSet<Material> worldBlacklistedMaterials = getBlacklistedMaterials(useCustomBlacklistedMaterials);

        // Debugger Start
        //LogConsole.info("Used config: " + useCustomBlacklistedMaterials + " and the values are : " + blacklistedMaterials,"info");
        // Debugger End

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

            if (worldBlacklistedMaterials.contains(location.getBlock().getType())) {
                continue;
            }

            return location.add(0.5d, 1d, 0.5d);
        }
    }
}
