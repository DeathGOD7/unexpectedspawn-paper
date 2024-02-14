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

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;

import static com.github.deathgod7.unexpectedspawn.Utils.*;

public class EventsHandler implements Listener {
	
	private final UnexpectedSpawn plugin;
	private final HashSet<World> blacklistedWorlds = new HashSet<>();
	
	public EventsHandler(UnexpectedSpawn plugin) {
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
			// add invulnerable to the player
			addInvulnerable(event.getPlayer(), joinWorld);
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		World respawnWorld = event.getRespawnLocation().getWorld();
		String wName = respawnWorld.getName();
		boolean returnFromEnd = false;

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

			// reset death world
			LogConsole.info("Resetting the death world.", LogConsole.logTypes.debug);
			deathWorld = null;
		}
		else {
			LogConsole.info("Using world (" + wName + ") where player will respawn normally. Probably coming back to OVERWORLD from END.", LogConsole.logTypes.debug);
			returnFromEnd = true;
		}

		if (blacklistedWorlds.contains(respawnWorld)) {
			LogConsole.info("User in blacklisted world ("+ respawnWorld +"). So random respawn is disabled.", LogConsole.logTypes.debug);
			return;
		}
		
		if (ApiUtil.isAvailable(PlayerRespawnEvent.class, "isAnchorSpawn") && event.isAnchorSpawn()) {
			return;
		}
		
		String useCustomOnDeath = checkWorldConfig(respawnWorld, "random-respawn.on-death");
		String useCustomOnReturnEND = checkWorldConfig(respawnWorld, "random-respawn.on-return-from-end");
		String useCustomBedRespawn = checkWorldConfig(respawnWorld, "random-respawn.bed-respawn-enabled");

		if ((!returnFromEnd && !plugin.config.getConfig().getBoolean(useCustomOnDeath + "random-respawn.on-death")) ||
				(returnFromEnd && !plugin.config.getConfig().getBoolean(useCustomOnReturnEND + "random-respawn.on-return-from-end"))
		) {
			return;
		}
		
		
		if (!event.isBedSpawn() || !plugin.config.getConfig().getBoolean(useCustomBedRespawn + "random-respawn.bed-respawn-enabled")) {
				Location respawnLocation = getRandomSpawnLocation(respawnWorld);
				event.setRespawnLocation(respawnLocation);
				// add invulnerable to the player
				addInvulnerable(event.getPlayer(), respawnWorld);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
			&& plugin.preventDmg.contains(event.getEntity().getUniqueId())
		) {
			LogConsole.warn("Player " + event.getEntity().getName() + " was going to take damamge after teleporting.", LogConsole.logTypes.debug);
			event.setCancelled(true);
			LogConsole.warn("Canceling damage done to player " + event.getEntity().getName() + ".", LogConsole.logTypes.debug);
		}
	}

}
