package com.virgilio.chat.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HelpCmd implements CommandExecutor {

	private ChatHelp plugin;
	private HashMap<String, TextComponent> placeholders;
	private List<String> menus;
	private List<String> replace;
	FileConfiguration config;
	int menuscount;

	public HelpCmd(ChatHelp plugin) {
		this.plugin = plugin;
		loadPlaceholders();
	}

	public void loadPlaceholders() {
		config = plugin.getConfig();
		menus = new ArrayList<String>();
		menuscount = 0;
		for (String unit : plugin.getConfig().getConfigurationSection("Menus").getKeys(false)) {
			menus.add(unit);
			menuscount++;
		}
		replace = new ArrayList<String>();
		for (String unit : plugin.getConfig().getConfigurationSection("Actions").getKeys(false)) {
			replace.add(unit);
		}

		placeholders = new HashMap<String, TextComponent>();
		int count = 0;
		for (String unit : plugin.getConfig().getConfigurationSection("Actions").getKeys(false)) {
			count++;
			TextComponent action = new TextComponent(TextComponent.fromLegacyText(getColorString("Actions." + unit + ".text")));
			if (plugin.getConfig().isSet("Actions." + unit + ".hover") && !plugin.getConfig().getString("Actions." + unit + ".hover").equals("")) {
				action.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(getColorString("Actions." + unit + ".hover"))));
			}
			if (plugin.getConfig().isSet("Actions." + unit + ".click") && !plugin.getConfig().getString("Actions." + unit + ".click").equals("")) {
				action.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(plugin.getConfig().getString("Actions." + unit + ".action")), plugin.getConfig().getString("Actions." + unit + ".click")));
			} 
			placeholders.put(unit, action);
		}
		System.out.println("[ChatHelpMenu] " + ChatColor.GREEN + "Loaded " + count + " placeholders...");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length > 1) {
			sender.sendMessage(ChatColor.DARK_RED + "Too many arguments");
			return false;
		}
		
		if (args.length == 1 && args[0].equals("reload")) {
			if (sender instanceof Player) {
				Player var2 = (Player) sender;
				if (var2.hasPermission("chathelpmenu.reload")) {
					plugin.reloadConfig();
					loadPlaceholders();
					var2.sendMessage(ChatColor.GREEN + "Reloaded config.yml");
				} else {
					var2.sendMessage(ChatColor.DARK_RED + "You don't have the permission to use this command");
				}
				return true;
			} else {
			plugin.reloadConfig();
			loadPlaceholders();
			sender.sendMessage(ChatColor.GREEN + "Reloaded config.yml");
			return true;
			}
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "You can't use this command in the console");
			return false;
		}

		Player player = (Player) sender;

		for (String menu : config.getConfigurationSection("Menus").getKeys(false)) {
			if (args.length == 0 && menu.equals(plugin.getConfig().getString("default-menu")) || args.length == 1 && args[0].equals(menu)) {
				for (String line : plugin.getConfig().getStringList("Menus." + menu)) {
					int count = getPlaceholderCount(line);
					if (count > 0) {

						String[] splits = line.split(" ");
						TextComponent test = new TextComponent();

						for (int i = 0; i < splits.length; i++) {
							if (replace.contains(splits[i])) {
								test.addExtra(placeholders.get(splits[i]));
								test.addExtra(" ");
							} else {
								boolean a = true;
								for (String string : replace) {
									if (splits[i].contains(string)) {
										a = false;
										String[] var1 = splits[i].trim().split(string);
										for (int z = 0; z < var1.length + 1; z++) {
										    if (z == 0) test.addExtra(ChatColor.translateAlternateColorCodes('&', var1[z]));
										    if (z == 1) test.addExtra(placeholders.get(string));
										    if (z == 2 && var1[z-1] == null) test.addExtra(" ");
										    if (z == 2) test.addExtra(ChatColor.translateAlternateColorCodes('&', var1[z-1] + " "));
										}
									}
								}
								if (a) {
									test.addExtra(ChatColor.translateAlternateColorCodes('&', splits[i]) + " ");
								}
							}
						}
						player.spigot().sendMessage(test);
					} else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
					}

				}
				return true;
			}
		}

		

		sender.sendMessage(ChatColor.DARK_RED + "Invalid help menu");
		return false;
	}

	public String[] getPlaceholders(String string) {
		String[] contained = new String[getPlaceholderCount(string)];
		int i = 0;
		for (Map.Entry<String, TextComponent> entry : placeholders.entrySet()) {
			if (string.contains(entry.getKey())) {
				contained[i] = entry.getKey();
				i++;
			}
		}
		return contained;
	}

	public int getPlaceholderCount(String string) {
		int count = 0;
		for (Map.Entry<String, TextComponent> entry : placeholders.entrySet()) {
			if (string.contains(entry.getKey())) {
				count++;
			}
		}
		return count;
	}

	public String getColorString(String string) {
		return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(string));
	}

}
