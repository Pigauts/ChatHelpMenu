package com.virgilio.chat.help;

import org.bukkit.plugin.java.JavaPlugin;

public class ChatHelp extends JavaPlugin {

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getCommand("help").setExecutor(new HelpCmd(this));
		
	}

}
