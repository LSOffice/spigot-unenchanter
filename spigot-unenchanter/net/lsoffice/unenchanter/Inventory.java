package net.lsoffice.unenchanter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Inventory implements Listener {

	public org.bukkit.inventory.Inventory gui(ItemStack item) {
		org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Unenchanter");
		int enchantCost = 0;

		if (item.equals(null)) {
			item = new ItemStack(Material.AIR, 1);
		}
		else {
			for (Enchantment e: item.getEnchantments().keySet()) {
				enchantCost += (int) ((item.getEnchantments().get(e))/2);
			}
		}

		ItemStack anvil = new ItemStack(Material.ANVIL);
		ItemMeta anvilmeta = anvil.getItemMeta();
		anvilmeta.setDisplayName("Disenchant Item");
		List<String> anvillore = new ArrayList<>();

		if (enchantCost != 0) {
			anvillore.add(ChatColor.WHITE + "");
			anvillore.add(ChatColor.RED + "This action will cost " + Integer.toString(enchantCost) + " XP levels");
		}
		else {
			anvillore.add(ChatColor.WHITE + "");
			anvillore.add(ChatColor.WHITE + "Add an item to the blank slot");
			anvillore.add(ChatColor.WHITE + "above to disenchant it");
		}
		anvilmeta.setLore(anvillore);
		anvil.setItemMeta(anvilmeta);

		gui.setItem(22, item);
		gui.setItem(40, anvil);

		for (int i = 0; i < gui.getSize(); i++) {
			if (gui.getItem(i) == null || gui.getItem(i).getType().equals(Material.AIR)) {
				gui.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1));
			}
		}

		return gui;
	}

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
		player.openInventory(gui(null));
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		if (!event.getView().getTitle().equals("Unenchanter")) return;
		if (!event.getView().getItem(40).getType().equals(Material.ANVIL)) return;
		
		if (player.getInventory().firstEmpty() == -1) {
			player.getWorld().dropItemNaturally(player.getLocation(), event.getView().getItem(22));
			player.sendMessage(ChatColor.RED + "Your inventory was full so an item was dropped onto the ground!");
		}
		else {
			player.getInventory().addItem(event.getView().getItem(22));
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!event.getView().getTitle().equals("Unenchanter")) return;
		if (!event.getView().getItem(40).getType().equals(Material.ANVIL)) return;

		if (!event.getClickedInventory().getItem(40).getType().equals(Material.ANVIL)) {
			if (!event.getClickedInventory().getHolder().equals(event.getWhoClicked())) {
				if (!(event.getSlot() == 22)) {
					event.setCancelled(true);

					if (event.getSlot() == 40) {
						if (event.getView().getItem(22).getType().equals(Material.AIR)) {
							event.getWhoClicked().closeInventory();
							event.getWhoClicked().sendMessage(ChatColor.RED + "You need to put an item in the black slot to disenchant it!");
							return;
						}
						else {
							int enchantCost = 0;

							if (event.getView().getItem(22).getEnchantments().isEmpty()) {
								event.getWhoClicked().closeInventory();
								event.getWhoClicked().sendMessage(ChatColor.RED + "This item does not have any enchantments!");
								return;
							}
							
							for (Enchantment e: event.getView().getItem(22).getEnchantments().keySet()) {
								enchantCost += (int) ((event.getView().getItem(22).getEnchantments().get(e))/2);
							}
							
							if (event.getWhoClicked().getExpToLevel() < enchantCost) {
								event.getWhoClicked().closeInventory();
								event.getWhoClicked().sendMessage(ChatColor.RED + "You don't have enough experience levels to do this! (" + Integer.toString(enchantCost) + " levels)");
								return;
							}
								
							Player player = (Player) event.getWhoClicked();
							player.setLevel(player.getExpToLevel() - enchantCost);
							
							for (Enchantment e: event.getView().getItem(22).getEnchantments().keySet()) {
								ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
								EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) book.getItemMeta();
								bookmeta.addStoredEnchant(e, event.getView().getItem(22).getEnchantments().get(e), true);
								book.setItemMeta(bookmeta);
								
								if (player.getInventory().firstEmpty() == -1) {
									player.getWorld().dropItemNaturally(player.getLocation(), book);
									player.sendMessage(ChatColor.RED + "Your inventory was full so an item was dropped onto the ground!");
								}
								else {
									player.getInventory().addItem(book);
								}
							}
							
							event.getView().getTopInventory().setItem(22, new ItemStack(event.getView().getItem(22).getType()));
							player.closeInventory();
						}
					}
				}
				else {
					event.getWhoClicked().openInventory(gui(event.getView().getItem(22)));
				}
			}
		}

	}

}
