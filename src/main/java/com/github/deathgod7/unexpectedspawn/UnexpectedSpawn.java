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

import org.bukkit.plugin.java.JavaPlugin;

public final class UnexpectedSpawn extends JavaPlugin {

    private static UnexpectedSpawn _instance;
    public static UnexpectedSpawn getInstance() {
        return _instance;
    }
    ConfigAccessor config;
    CommandHandler commandHandler;

    @Override
    public void onEnable() {
        _instance = this;
        // load default config
        this.config = new ConfigAccessor(this, "config.yml");
        this.config.getConfig().options().copyDefaults(true);

        // save default config to file
        this.config.saveDefaultConfig();

        if (this.config.getConfig().contains("version")) {
            this.config.getConfig().set("version", this.getDescription().getVersion());
        }

        // register commands
        this.commandHandler = new CommandHandler(this);
        this.commandHandler.RegisterCommands();

        // register events
        getServer().getPluginManager().registerEvents(new EventHandler(this), this);
    }

}
