package com.isekai.lootbox;

import com.isekai.lootbox.commands.LootboxCommand;
import com.isekai.lootbox.listeners.GUIListener;
import com.isekai.lootbox.listeners.LootboxUseListener;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.managers.LootboxManager;
import com.isekai.lootbox.managers.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LootboxPlugin extends JavaPlugin {

    private static LootboxPlugin instance;
    private ConfigManager configManager;
    private LootboxManager lootboxManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Create data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Initialize managers (order matters!)
        configManager = new ConfigManager(this);
        lootboxManager = new LootboxManager(this);
        lootboxManager.loadLootboxes();
        playerDataManager = new PlayerDataManager(this);
        
        // Register commands
        getCommand("lootbox").setExecutor(new LootboxCommand(this));
        getCommand("lootbox").setTabCompleter(new LootboxCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new LootboxUseListener(this), this);
        
        getLogger().info("LootboxPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (lootboxManager != null) {
            lootboxManager.saveLootboxes();
        }
        if (playerDataManager != null) {
            playerDataManager.saveData();
        }
        getLogger().info("LootboxPlugin has been disabled!");
    }

    public static LootboxPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LootboxManager getLootboxManager() {
        return lootboxManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public void reloadPlugin() {
        configManager.loadConfig();
        lootboxManager.loadLootboxes();
        playerDataManager.loadData();
    }
}
