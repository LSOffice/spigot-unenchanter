package net.lsoffice.unenchanter;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import net.md_5.bungee.api.ChatColor;

public class Inventory implements Listener {
	
	HashMap<Player, ItemStack> textMessage = new HashMap<Player, ItemStack>();

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if (!event.getClickedBlock().getType().equals(Material.GRINDSTONE)) return;

		Player player = event.getPlayer();
		if (!Commands.toggled.contains(player)) return;
		event.setCancelled(true);
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
			player.sendMessage(ChatColor.RED + "You cannot be in creative or spectator to use this menu!");
			return;
		}

		int enchantCost = 0;

		for (Enchantment e: player.getInventory().getItemInMainHand().getEnchantments().keySet()) {
			enchantCost += player.getInventory().getItemInMainHand().getEnchantments().get(e);
		}
		
		if (enchantCost == 0) {
			player.sendMessage(ChatColor.RED + "This item does not have any enchantments!");
			return;
		}

		String itemName = player.getInventory().getItemInMainHand().getType().name();
		if (player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) itemName = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
		player.sendMessage(ChatColor.WHITE + "");
		player.sendMessage(ChatColor.AQUA + "Do you want to unenchant this item: " + itemName);
		player.sendMessage(ChatColor.AQUA + "This will cost " + enchantCost + " levels!");
		player.sendMessage(ChatColor.WHITE + "");
		player.sendMessage(ChatColor.GREEN + "Type anything in chat to confirm this action, or type cancel to cancel");
		player.sendMessage(ChatColor.WHITE + "");
		textMessage.put(player, player.getInventory().getItemInMainHand());
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (!textMessage.containsKey(event.getPlayer())) return;
		Player player = event.getPlayer();
		ItemStack item = textMessage.get(player);
		textMessage.remove(player);
		event.setCancelled(true);
		if (!player.getInventory().getItemInMainHand().equals(item)) {
			player.sendMessage(ChatColor.RED + "Do not change the item you are holding while unenchanting! Unenchanting failed");
			return;
		}
		
		if (event.getMessage().equalsIgnoreCase("cancel")) {
			player.sendMessage(ChatColor.RED + "This action was cancelled! Unenchanting failed");
			return;
		}
		
		int enchantCost = 0;

		for (Enchantment e: item.getEnchantments().keySet()) {
			enchantCost += item.getEnchantments().get(e);
		}
		
		int playerEXP = (player.getExpToLevel() - 7) / 2;
		if (playerEXP < enchantCost) {
			player.sendMessage(ChatColor.RED + "You do not have enough XP levels to complete this action (" + enchantCost + " XP levels)");
			return;
		}
		
		player.setLevel(playerEXP - enchantCost);
		for (Enchantment e:item.getEnchantments().keySet()) {
			ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
			EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) book.getItemMeta();
			bookmeta.addStoredEnchant(e, item.getEnchantments().get(e), true);
			book.setItemMeta(bookmeta);

			if (player.getInventory().firstEmpty() == -1) {
				player.getWorld().dropItemNaturally(player.getLocation(), book);
				player.sendMessage(ChatColor.RED + "Your inventory was full so an item was dropped onto the ground!");
			}
			else {
				player.getInventory().addItem(book);
			}
		}
		
		player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		if (player.getInventory().firstEmpty() == -1) {
			player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(item.getType()));
			player.sendMessage(ChatColor.RED + "Your inventory was full so an item was dropped onto the ground!");
		}
		else {
			player.getInventory().addItem(new ItemStack(item.getType()));
		}
		player.sendMessage(ChatColor.GREEN + "Item was successfully unenchanted!");
	}

}
