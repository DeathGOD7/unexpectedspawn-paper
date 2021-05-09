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

    Spawn(UnexpectedSpawn plugin) {
        this.plugin = plugin;
        List<String> materialList = plugin.config.getConfig().getStringList("spawn-block-blacklist");
        for (String name : materialList) {
            Material material = Material.getMaterial(name);
            if (material == null) {
                Bukkit.getLogger().warning("Material " + name + " is not valid. See https://papermc.io/javadocs/paper/org/bukkit/Material.html");
                continue;
            }
            blacklistedMaterial.add(material);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World joinWorld = event.getPlayer().getWorld();
        if (joinWorld.getEnvironment().equals(World.Environment.NETHER)
                || joinWorld.getEnvironment().equals(World.Environment.THE_END)) {
            return;
        }
        if ((!event.getPlayer().hasPlayedBefore() && plugin.config.getConfig().getBoolean("first-join-only"))
                || plugin.config.getConfig().getBoolean("on-join")) {
            Location joinLocation = getRandomSpawnLocation(joinWorld);
            event.getPlayer().teleport(joinLocation);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
            return;
        }

        if (!event.isBedSpawn() || !plugin.config.getConfig().getBoolean("bed-respawn-enabled")) {
            World respawnWorld = event.getRespawnLocation().getWorld();
            Location respawnLocation = getRandomSpawnLocation(respawnWorld);
            event.setRespawnLocation(respawnLocation);
        }
    }

    private Location getRandomSpawnLocation(World world) {
        int xmin = plugin.config.getConfig().getInt("x-min");
        int xmax = plugin.config.getConfig().getInt("x-max");
        int zmin = plugin.config.getConfig().getInt("z-min");
        int zmax = plugin.config.getConfig().getInt("z-max");
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

            if (blacklistedMaterial.contains(location.getBlock().getType())) {
                continue;
            }

            return location.add(0.5d, 1d, 0.5d);
        }
    }
}
