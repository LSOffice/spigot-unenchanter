package net.lsoffice.unenchanter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	static JavaPlugin plugin;
	static String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
	static String majorVer = split[0];
	static String minorVer = split[1];
	static String minorVer2 = split.length > 2 ? split[2]:"0";
	FileConfiguration config = this.getConfig();
	
	@Override
	public void onEnable() {
		plugin = this;
		config.addDefault("needpermission", false);
		
		getServer().getPluginManager().registerEvents(new Inventory(), this);
		getCommand("unenchanter").setExecutor(new Commands());
		
		getServer().getConsoleSender().sendMessage("[Unenchanter] Plugin was enabled");
		
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("[Unenchanter] Plugin was disabled");
	}
}

