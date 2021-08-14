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

import org.bukkit.plugin.java.JavaPlugin;

public final class UnexpectedSpawn extends JavaPlugin {

    ConfigAccessor config;

    @Override
    public void onEnable() {
        this.config = new ConfigAccessor(this, "config.yml");
        this.config.getConfig().options().copyDefaults(true);
//        config.saveConfig();
//        used this so the comment in config.yml is preserved.
        this.config.saveDefaultConfig();

        getCommand("unexpectedspawn").setExecutor(new Reload(this));
        getServer().getPluginManager().registerEvents(new Spawn(this), this);
    }

}
