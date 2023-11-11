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

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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
		InvulnerableTime("invulnerable-duration"),
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

	/**
	 * Checks if the world config is available (per-world config override)
	 * @param world The world to look for
	 * @param config The config string (some can also be obtained from {@link ConfigVariable})
	 * @return {@link String}
	 */
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

	/**
	 * Adds fail radius based on its number sign
	 * @param previous The number to add on
	 * @param rangetoadd The number to add (fail radius)
	 * @return {@link int}
	 */
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

	/**
	 * <pre>Gets the area value (eg: XMin, ZMax..etc) from config file based on world. Also used to parse the string nymber from rtp command </pre>
	 * @param value Used in parsing the string (should be null if want to get value from config)
	 * @param label Label to get value of (eg: XMin, ZMax..etc)
	 * @param world World to look in config for value
	 * @return {@link int}
	 */
	public static int getAreaValue(@Nullable String value, ConfigVariable label, World world) {
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

	/**
	 * Gets the blacklist materials invert status of world
	 * @param label The invert variable of config file
	 * @param world The world to in config for value
	 * @return {@link boolean}
	 */
	public static boolean getInvertStatus(ConfigVariable label, World world) {
		String custom = checkWorldConfig(world, label.configstring);
		boolean temp = plugin.config.getConfig().getBoolean(custom + label.configstring);
		LogConsole.info("Used config: " + custom + " and the blacklist invert is " + temp + " in world \"" + world.getName() + "\"", LogConsole.logTypes.debug);
		return temp;
	}

	/**
	 * Gets all the blacklish / whitelist materials from config
	 * @param label The label in config to look for [based on {@link ConfigVariable}]
	 * @param world The world to get config of
	 * @return {@link HashSet<Material>}
	 */
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

	/**
	 * Used to colorize string based on '&..' color codes
	 * @param string The string to colorize
	 * @return {@link String}
	 */
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
			int y;

			Location location;

			if (world.getEnvironment().equals(World.Environment.NETHER)) {
				// for nether
				int minY = 0;
				int maxY = 128;
				location = getLocAtNether(x, z, minY, maxY, world, spawnBlacklistedMaterials, isSpawnBlacklistInverted);
			}else {
				// for overworld and normal worlds
				location = getLocAtNormal(x, z, world, spawnBlacklistedMaterials, isSpawnBlacklistInverted);
			}
			tryCount++;

			if (location == null) {
				continue;
			}

			y = location.getBlockY();

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
			int y;

			Location location;

			if (world.getEnvironment().equals(World.Environment.NETHER)) {
				// for nether
				int minY = 0;
				int maxY = 128;
				location = getLocAtNether(x, z, minY, maxY, world, spawnBlacklistedMaterials, isSpawnBlacklistInverted);
			}else {
				// for overworld and normal worlds
				location = getLocAtNormal(x, z, world, spawnBlacklistedMaterials, isSpawnBlacklistInverted);
			}
			tryCount++;

			if (location == null) {
				continue;
			}

			y = location.getBlockY();

			LogConsole.warn("Found location for random respawn after "+tryCount+" tries (X "+x+", Y "+y+", Z "+z+")", LogConsole.logTypes.log);

			return location.add(0.5d, 1d, 0.5d);
		}
	}

	/**
	 * Checks if the loop should continue to look for new lcoation
	 * @param material The material to look for
	 * @param blacklistedMaterials The blacklisted materials or whitelisted (if invert is true)
	 * @param invertStatus Status of invert (true then whitelist, false then blacklist)
	 * @return {@link boolean}
	 */
	private static boolean checkMaterialsContinue(Material material, HashSet<Material> blacklistedMaterials, boolean invertStatus){
		if(!invertStatus) { // if not inverted => BLACKLIST
			// returns true if blacklist contains that block material
			// returns false if blacklist doesn't contain that block material
			return blacklistedMaterials.contains(material);
		}
		else { // if list is inverted => WHITELIST
			// returns false if blacklist contains that block material
			// returns true if blacklist doesn't contain that block material
			return !blacklistedMaterials.contains(material);
		}
	}

	/**
	 * Gets the normal world (overworld and end) random location
	 * @param x The X co-ordinate of location (from random generator)
	 * @param z The Z co-ordinate of location (from random generator)
	 * @param world The world to get location of
	 * @param spawnBlacklistedMaterials The blacklisted materials or whitelisted (if invert is true)
	 * @param isSpawnBlacklistInverted Status of invert (true then whitelist, false then blacklist)
	 * @return {@link Location}
	 */
	private static Location getLocAtNormal(int x, int z, World world , HashSet<Material> spawnBlacklistedMaterials, boolean isSpawnBlacklistInverted) {
		int y = world.getHighestBlockYAt(x, z);
		Location location = new Location(world, x, y, z);

		// Special case for server version < 1.15.2 (?)
		// Related: https://www.spigotmc.org/threads/gethighestblockat-returns-air.434090/
		if (location.getBlock().getType() == Material.AIR) {
			location = location.subtract(0, 1, 0);
		}

		// if block is solid and [not in blacklist or is in whitelist]
		if (checkMaterialsContinue(location.getBlock().getType(), spawnBlacklistedMaterials, isSpawnBlacklistInverted)){
			return null;
		}

		return  location;
	}

	/**
	 * Gets the nether world random location
	 * @param x The X co-ordinate of location (from random generator)
	 * @param z The Z co-ordinate of location (from random generator)
	 * @param minY The minimum Y height (usually 0)
	 * @param maxY The maximum Y height (usually 128)
	 * @param world The world to get location of
	 * @param spawnBlacklistedMaterials The blacklisted materials or whitelisted (if invert is true)
	 * @param isSpawnBlacklistInverted Status of invert (true then whitelist, false then blacklist)
	 * @return {@link Location}
	 */
	private static Location getLocAtNether(int x, int z, int minY, int maxY, World world,  HashSet<Material> spawnBlacklistedMaterials, boolean isSpawnBlacklistInverted) {
		for (int y = minY + 1; y < maxY; y++) {
			Block currentBlock = world.getBlockAt(x, y, z);
			if (currentBlock.getType().name().endsWith("AIR") || !currentBlock.getType().isSolid()) { // current block is air
				if (!currentBlock.getType().name().endsWith("AIR") && !currentBlock.getType().isSolid()) { // current block is not air nor solid (ex: lava, water...)
					Material currentBlockMaterial = currentBlock.getType();
					if (checkMaterialsContinue(currentBlockMaterial, spawnBlacklistedMaterials, isSpawnBlacklistInverted)){
						continue;
					}
				}

				// current block is infact empty air
				// now check if below block is solid [a.k.a landing block] or not
				Block belowblock = world.getBlockAt(x, y - 1, z);
				Material belowBlockMaterial = belowblock.getType();
				if (belowBlockMaterial.name().endsWith("AIR")) {
					// if block below is air, skip
					continue;
				}

				// if below block is solid and [not in blacklist or is in whitelist]
				// and above block is empty (AIR) for headspace
				if (world.getBlockAt(x, y + 1, z).getType().name().endsWith("AIR") //Head block => empty / air
						&& !checkMaterialsContinue(belowBlockMaterial, spawnBlacklistedMaterials, isSpawnBlacklistInverted)) //Valid block
					return new Location(world, x, y, z);
			}
		}
		return null;
	}


	public static void addInvulnerable(Player player, World world) {
		// add player UUID to check for preventing damage
		if (plugin.preventDmg.add(player.getUniqueId())) {
			LogConsole.warn("Player " + player.getName() + " will now turn into immortal peasant.", LogConsole.logTypes.debug);
		}

		// start timer for invulnerable
		ConfigVariable label = ConfigVariable.InvulnerableTime;
		String custom = checkWorldConfig(world, label.configstring);
		int sec = plugin.config.getConfig().getInt(custom + label.configstring);
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			LogConsole.warn("Prevent DMG (List) : " + plugin.preventDmg.toString(), LogConsole.logTypes.debug);
			// remove player UUID after if no fall damage after 5s of teleport
			if (plugin.preventDmg.contains(player.getUniqueId())) {
				plugin.preventDmg.remove(player.getUniqueId());
				LogConsole.warn("Player " + player.getName() + " will now turn into mortal servant.", LogConsole.logTypes.debug);
			}
		}, sec * 20L);

	}

}
