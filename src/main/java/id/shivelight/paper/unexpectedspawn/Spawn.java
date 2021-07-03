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

import com.destroystokyo.paper.HeightmapType;
import io.papermc.paper.world.MoonPhase;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

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

        if (blackListedWorlds.contains(joinWorld)) {
            // Debugger Start
            //logMessageConsole("User in blacklisted world["+ joinWorld +"]. So random join spawn is disabled.","info");
            // Debugger End
            return;
        }

        String useCustomOnFirstJoin = checkWorldConfig(joinWorld, "random-respawn.on-first-join");
        String useCustomAlwaysOnJoin = checkWorldConfig(joinWorld, "random-respawn.always-on-join");

        // Debugger Start
        //logMessageConsole("Used config: " + useCustomOnFirstJoin + " for first join and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnFirstJoin + "random-respawn.on-first-join"), "info");
        //logMessageConsole("Used config: " + useCustomAlwaysOnJoin + " for always on join and the values is "+ plugin.config.getConfig().getBoolean(useCustomAlwaysOnJoin + "random-respawn.always-on-join"), "info");
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
                logMessageConsole("Using world ("+ wName +") where player died.","info");
            }
            else {
                World obtainedWorld = Bukkit.getWorld(obtainedData);
                if (obtainedWorld == null) {
                    logMessageConsole("Couldn't find world "+ obtainedData + ". Either it doesn't exist or is not valid.","warn");
                    respawnWorld = deathWorld;;
                    String wName = respawnWorld.getName();
                    logMessageConsole("Using world ("+ wName +") where player died.","info");
                }
                else {
                    respawnWorld = obtainedWorld;

                    String wName = respawnWorld.getName();
                    logMessageConsole("Using world ("+ wName +") specified in config.","info");
                }
            }
        }
        else {
            logMessageConsole("Respawn World in ("+useCustomWorld+") cannot be null. Please add empty string to disable it.","severe");
        }

        if (blackListedWorlds.contains(respawnWorld)) {
            // Debugger Start
            //logMessageConsole("User in blacklisted world["+ respawnWorld +"]. So random respawn is disabled.","info");
            // Debugger End
            return;
        }

        if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
            return;
        }

        String useCustomOnDeath = checkWorldConfig(respawnWorld, "random-respawn.on-death");
        String useCustomBedRespawn = checkWorldConfig(respawnWorld, "random-respawn.bed-respawn-enabled");

        // Debugger Start
        //logMessageConsole("Used config: " + useCustomOnDeath + " for on death and the values is "+ plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death"), "info");
        //logMessageConsole("Used config: " + useCustomBedRespawn + " for bed respawn and the values is "+ plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled"), "info");
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

        // Debugger Start
        //logMessageConsole("Used config: " + useCustomMinX + " for random respawn area and the values are ("+xmin+","+xmax+","+zmin+","+zmax+")","info");
        // Debugger End

        String useCustomBlacklistedMaterials = checkWorldConfig(world, "spawn-block-blacklist");
        HashSet<Material> blacklistedMaterials = getBlacklistedMaterials(useCustomBlacklistedMaterials);

        // Debugger Start
        //logMessageConsole("Used config: " + useCustomBlacklistedMaterials + " and the values are : " + blacklistedMaterials,"info");
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

            if (blacklistedMaterials.contains(location.getBlock().getType())) {
                continue;
            }

            return location.add(0.5d, 1d, 0.5d);
        }
    }
}
