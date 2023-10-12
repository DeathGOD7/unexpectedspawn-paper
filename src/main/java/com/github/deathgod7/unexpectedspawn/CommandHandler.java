// This file is part of unexpectedspawn, created on 09/10/2023 (20:31 PM)
// Name : CommandHandler
// Author : Death GOD 7

package com.github.deathgod7.unexpectedspawn;

public class CommandHandler {
	UnexpectedSpawn plugin;

	public CommandHandler(UnexpectedSpawn instance) {
		this.plugin = instance;
	}

	public void RegisterCommands(){
		// main command "/unexpectedspawn"
		this.plugin.getCommand("unexpectedspawn").setExecutor(new MainCommand(this.plugin));
	}

}


