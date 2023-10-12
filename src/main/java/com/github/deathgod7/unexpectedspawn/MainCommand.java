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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.deathgod7.unexpectedspawn.Utils.*;

public class MainCommand implements CommandExecutor, TabCompleter {
    UnexpectedSpawn plugin;

    MainCommand(UnexpectedSpawn plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("unexpectedspawn")) {
            if (args.length == 0){
                if (sender.hasPermission("unexpectedspawn.use")) {
                    String authors = String.join(", ", plugin.getDescription().getAuthors());
                    String version = plugin.getDescription().getVersion();
                    sender.sendMessage(Utils.colorize("UnexpectedSpawn Version : &8" + version));
                    sender.sendMessage(Utils.colorize("Authors: &8" + authors));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                // trigger help
                return PluginHelp(sender, label, args);
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                // trigger reload
                return ReloadPlugin(sender, args);
            }
            else if (args[0].equalsIgnoreCase("randomtp") || args[0].equalsIgnoreCase("rtp")) {
                //trigger rtp
                return RandomTP(sender, args);
            }
            else {
                // By returning false, server will send out available commands.
                return !sender.hasPermission("unexpectedspawn.use");
            }

        }
        return true;
    }

    private boolean PluginHelp(CommandSender sender, String label, String[] args) {
		if (args.length == 1 || args.length > 2) {
			String mainHelpMsg = "Use /<command> help [subcommand] for more information\n" +
					"/<command> help = Brings this help menu\n" +
					"/<command> randomtp = Used to force random tp\n" +
					"/<command> reload = Reloads the plugin";
			sender.sendMessage(mainHelpMsg.replace("<command>", label));
		}
		else {
			if (args[1].equalsIgnoreCase("randomtp") || args[1].equalsIgnoreCase("rtp")) {
				String mainHelpMsg = "Syntax : /<command> randomtp <w:worldname> <xmin:value> <xmax:value> <zmin:value> <zmax:value> [p:player]\n" +
						"<...> = Optional\n" +
						"[...] = Required if command is from console\n" +
						"Example : /<command> " + args[1].toLowerCase() + " w:myworld xmin:-250 xmax:500 zmin:-500 zmax:250 p:Steve";
				sender.sendMessage(mainHelpMsg.replace("<command>", label));
			} else {
				sender.sendMessage("Do you really need help for other commands? Like really??");
			}
		}
        return true;
    }
    private boolean ReloadPlugin(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return !sender.hasPermission("unexpectedspawn.use.reload");
        }
        else {
            if (sender.hasPermission("unexpectedspawn.use.reload")) {
                plugin.config.reloadConfig();
                sender.sendMessage(Utils.colorize("&8UnexpectedSpawn reloaded!"));
                return true;
            }
        }
        return true;
    }
	
    private boolean RandomTP(CommandSender sender, String[] args) {
		if (sender.hasPermission("unexpectedspawn.use.randomtp")) {
			// <command> randomtp w:worldname xmin:value xmax:value zmin:value zmax:value p:player
			String stringWorld = null;
			String stringXMin = null;
			String stringXMax = null;
			String stringZMin = null;
			String stringZMax = null;
			String stringPlayer = null;
			
			// Iterate through the arguments
			for (String arg : args) {
				String formattedArg = arg.toLowerCase();
				if (formattedArg.startsWith("w:")) {
					stringWorld = arg.substring(2);
				}
				else if (formattedArg.startsWith("xmin:")) {
					stringXMin = arg.substring(5);
				}
				else if (formattedArg.startsWith("xmax:")) {
					stringXMax = arg.substring(5);
				}
				else if (formattedArg.startsWith("zmin:")) {
					stringZMin = arg.substring(5);
				}
				else if (formattedArg.startsWith("zmax:")) {
					stringZMax = arg.substring(5);
				}
				else if (formattedArg.startsWith("p:")) {
					stringPlayer = arg.substring(2);
				}
			}

			LogConsole.info("[ARGS]", LogConsole.logTypes.debug);
			LogConsole.info("World Name: " + stringWorld, LogConsole.logTypes.debug);
			LogConsole.info("X Min: " + stringXMin, LogConsole.logTypes.debug);
			LogConsole.info("X Max: " + stringXMax, LogConsole.logTypes.debug);
			LogConsole.info("Z Min: " + stringZMin, LogConsole.logTypes.debug);
			LogConsole.info("Z Max: " + stringZMax, LogConsole.logTypes.debug);
			LogConsole.info("Player Name: " + stringPlayer, LogConsole.logTypes.debug);
			
			// values
			World world;
			Player player;
			int xmin;
			int xmax;
			int zmin;
			int zmax;
			
			// console check
			if (stringPlayer == null && sender instanceof ConsoleCommandSender) {
				// must have player if sent from console
				LogConsole.warn("Player field in arg was empty. I can try teleporting some brain parts to you.", LogConsole.logTypes.debug);
				sender.sendMessage("Must include player if sent from console!");
				return false;
			}
			
			// player
			if (stringPlayer == null) {
				player = (Player) sender;
			}
			else {
				player = plugin.getServer().getPlayer(stringPlayer);
				if (player == null) {
					LogConsole.warn("The player provided seems to be magician!! POOF!!", LogConsole.logTypes.debug);
					sender.sendMessage("Couldn't find the player. Please check again.");
					return false;
				}
			}
			
			// world
			if (stringWorld == null) {
				world = player.getWorld();
			}
			else {
				world = plugin.getServer().getWorld(stringWorld);
				if (world == null) {
					LogConsole.warn("The world provided seems to be many light years further!! CAN'T REA...", LogConsole.logTypes.debug);
					sender.sendMessage("Couldn't find the world. Please check again.");
					return false;
				}
			}
			
			// area values
			xmin = getAreaValue(stringXMin, ConfigVariable.XMin, world);
			xmax = getAreaValue(stringXMax, ConfigVariable.XMax, world);
			zmin = getAreaValue(stringZMin, ConfigVariable.ZMin, world);
			zmax = getAreaValue(stringZMax, ConfigVariable.ZMax, world);

			// get random location
			Location randomLocation = getRandomSpawnLocation(xmin, xmax, zmin, zmax, world);

			// teleport user to it
			player.teleport(randomLocation);
			LogConsole.warn("Player has been thrown at random place to be tarnished and become maidenless!", LogConsole.logTypes.debug);
			sender.sendMessage("Player has been teleported randomly!");

		}

		// if no perms just default to nothing or after it finishes the whole code
        return  true;

    }


	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
		if (strings.length == 1) {
			List<String> subcommands = new ArrayList<>();

			if(sender.hasPermission("unexpectedspawn.use")){
				subcommands.add("help");
			}

			if (sender.hasPermission("unexpectedspawn.use.reload")) {
				subcommands.add("reload");
			}

			if (sender.hasPermission("unexpectedspawn.use.randomtp")) {
				subcommands.add("rtp");
				subcommands.add("randomtp");
			}

			return StringUtil.copyPartialMatches(strings[0], subcommands, new ArrayList<>());
		} else if (strings.length >= 2 && (strings[0].equalsIgnoreCase("rtp") || strings[0].equalsIgnoreCase("randomtp")) ) {
			List<String> subcommands = new ArrayList<>();

			// <command> randomtp w:worldname xmin:value xmax:value zmin:value zmax:value p:player
			if (sender.hasPermission("unexpectedspawn.use.randomtp")) {
				subcommands.add("w:");
				subcommands.add("xmin:");
				subcommands.add("xmax:");
				subcommands.add("zmin:");
				subcommands.add("zmax:");
				subcommands.add("p:");
			}

			return StringUtil.copyPartialMatches(strings[strings.length - 1], subcommands, new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
