package com.lonelydime.Rifts;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Warning {
	Rifts plugin;
	
	public Warning(Rifts instance) {
		plugin = instance;
	}
	
	public void checkDistances() {
		Character character, testChar;

		for (Player player:plugin.getServer().getOnlinePlayers()) {
			if (plugin.characters.containsKey(player)) {
				character = plugin.characters.get(player);
				for (Player test:plugin.getServer().getOnlinePlayers()) {
					if (plugin.characters.containsKey(test)) {
						testChar = plugin.characters.get(test);
						if (!character.getFaction().matches(testChar.getFaction()) && player.getLocation().getWorld().equals(test.getLocation().getWorld())) {
							if (!testChar.isSneaking())
								player.sendMessage(ChatColor.DARK_RED+test.getDisplayName()+" is close to you!");
							
							if (!character.isSneaking())
								test.sendMessage(ChatColor.DARK_RED+player.getDisplayName()+" is close to you!");
						}
					}
				}	
			}
		}
	}
}
