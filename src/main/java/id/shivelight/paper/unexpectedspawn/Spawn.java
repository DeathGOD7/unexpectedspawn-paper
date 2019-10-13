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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Random;
import java.util.UUID;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

public class Spawn implements Listener {

    private UnexpectedSpawn plugin;

    Spawn(UnexpectedSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        try{
            World joinWorld = event.getPlayer().getWorld();
            if (joinWorld.getEnvironment().equals(World.Environment.NETHER)
                || joinWorld.getEnvironment().equals(World.Environment.THE_END)) {
                return;
            }
            if (plugin.config.getConfig().getBoolean("on-join")){
                Location joinLocation = getRandomSpawnLocation(joinWorld);
                event.getPlayer().teleport(joinLocation);
            }
            else{
                UUID id = event.getPlayer().getUniqueId();
                if (event.getPlayer().hasPlayedBefore()){
                    File file = new File("plugins\\UnexpectedSpawn\\" + id.toString() + ".yml");
                    if (file.exists()){
                        Yaml store = new Yaml();
                        FileInputStream stream = new FileInputStream(file);
                        Location joinLocation = store.load(stream);
                        event.getPlayer().removeMetadata("UnexceptedSpawn.SpawnLocation", plugin);
                        event.getPlayer().setMetadata("UnexceptedSpawn.SpawnLocation", new org.bukkit.metadata.FixedMetadataValue(plugin, joinLocation));
                    }
                }
                else if (plugin.config.getConfig().getBoolean("first-join-only")){
                    Location joinLocation = getRandomSpawnLocation(joinWorld);
                    Yaml store = new Yaml();
                    File file = new File("plugins\\UnexpectedSpawn\\" + id.toString() + ".yml");
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(store.dump(joinLocation).getBytes());
                    stream.close();
                    event.getPlayer().removeMetadata("UnexceptedSpawn.SpawnLocation", plugin);
                    event.getPlayer().setMetadata("UnexceptedSpawn.SpawnLocation", new org.bukkit.metadata.FixedMetadataValue(plugin, joinLocation));
                    event.getPlayer().teleport(joinLocation);
                }
            }
        }
        catch (Exception ex){
            
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn() || !plugin.config.getConfig().getBoolean("bed-respawn-enabled")) {
            World respawnWorld = event.getRespawnLocation().getWorld();
            if (plugin.config.getConfig().getBoolean("first-join-only")){
                List<org.bukkit.metadata.MetadataValue> values = event.getPlayer().getMetadata("UnexceptedSpawn.SpawnLocation");
                if (values.size() > 0){
                    Location respawnLocation = (Location)values.get(0).value();
                    event.setRespawnLocation(respawnLocation);
                }
            }
            else{
                Location respawnLocation = getRandomSpawnLocation(respawnWorld);
                event.setRespawnLocation(respawnLocation);
            }
        }
    }

    private Location getRandomSpawnLocation(World world) {
        Random random = new Random();
        int xmin = plugin.config.getConfig().getInt("x-min");
        int xmax = plugin.config.getConfig().getInt("x-max");
        int zmin = plugin.config.getConfig().getInt("z-min");
        int zmax = plugin.config.getConfig().getInt("z-max");
        int x = xmin + random.nextInt((xmax - xmin) + 1);
        int z = zmin + random.nextInt((zmax - zmin) + 1);

        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, (double) x, (double) y, (double) z);
    }
}
