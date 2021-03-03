package net.lsoffice.unenchanter;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {
	static ArrayList<Player> toggled = new ArrayList<Player>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equalsIgnoreCase("unenchanter")) return true;
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use this command.");
			return true;
		}
		Player player = (Player) sender;
		
		if (toggled.contains(player)) {
			player.sendMessage(ChatColor.RED + "You have untoggled the unenchanter");
			toggled.remove(player);
		}
		else {
			player.sendMessage(ChatColor.GREEN + "You have toggled the unenchanter. All stonecutters will now open the unenchanter GUI");
			toggled.add(player);
		}
		
		return false;
	}

}
