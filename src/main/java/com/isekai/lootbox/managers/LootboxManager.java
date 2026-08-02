package com.isekai.lootbox.managers;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootboxManager {

    private final LootboxPlugin plugin;
    private final Map<String, Lootbox> lootboxes;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final NamespacedKey lootboxKey;

    public LootboxManager(LootboxPlugin plugin) {
        this.plugin = plugin;
        this.lootboxes = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "lootboxes.yml");
        this.lootboxKey = new NamespacedKey(plugin, "lootbox");
    }

    public void loadLootboxes() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create lootboxes.yml!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        lootboxes.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("lootboxes");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            ConfigurationSection lootboxSection = section.getConfigurationSection(name);
            if (lootboxSection == null) continue;

            int rows = lootboxSection.getInt("rows", 3);
            Lootbox lootbox = new Lootbox(name, rows);

            // Load type
            String type = lootboxSection.getString("type", plugin.getConfigManager().getDefaultType());
            lootbox.setType(type);

            // Load reward limit
            int rewardLimit = lootboxSection.getInt("reward-limit", plugin.getConfigManager().getDefaultRewardLimit());
            lootbox.setRewardLimit(rewardLimit);

            // Load display item
            if (lootboxSection.contains("displayItem")) {
                ItemStack displayItem = lootboxSection.getItemStack("displayItem");
                if (displayItem != null) {
                    lootbox.setDisplayItem(displayItem);
                }
            }

            // Load rewards
            List<?> rewardsList = lootboxSection.getList("rewards");
            if (rewardsList != null) {
                for (Object obj : rewardsList) {
                    if (obj instanceof ItemStack) {
                        lootbox.addReward((ItemStack) obj);
                    }
                }
            }

            lootboxes.put(name.toLowerCase(), lootbox);
        }

        plugin.getLogger().info("Loaded " + lootboxes.size() + " lootboxes!");
    }

    public void saveLootboxes() {
        dataConfig = new YamlConfiguration();

        for (Map.Entry<String, Lootbox> entry : lootboxes.entrySet()) {
            Lootbox lootbox = entry.getValue();
            String path = "lootboxes." + lootbox.getName();

            dataConfig.set(path + ".rows", lootbox.getRows());
            dataConfig.set(path + ".type", lootbox.getTypeName());
            dataConfig.set(path + ".reward-limit", lootbox.getRewardLimit());
            dataConfig.set(path + ".displayItem", lootbox.getDisplayItem());
            dataConfig.set(path + ".rewards", lootbox.getRewards());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save lootboxes.yml!");
            e.printStackTrace();
        }
    }

    public Lootbox createLootbox(String name, int rows) {
        if (lootboxes.containsKey(name.toLowerCase())) {
            return null;
        }
        Lootbox lootbox = new Lootbox(name, rows);
        lootbox.setType(plugin.getConfigManager().getDefaultType());
        lootbox.setRewardLimit(plugin.getConfigManager().getDefaultRewardLimit());
        lootboxes.put(name.toLowerCase(), lootbox);
        saveLootboxes();
        return lootbox;
    }

    public Lootbox getLootbox(String name) {
        return lootboxes.get(name.toLowerCase());
    }

    public boolean deleteLootbox(String name) {
        if (lootboxes.remove(name.toLowerCase()) != null) {
            // Clear any pending claims for this lootbox
            plugin.getPlayerDataManager().clearLootboxClaims(name);
            saveLootboxes();
            return true;
        }
        return false;
    }

    public List<String> getLootboxNames() {
        return new ArrayList<>(lootboxes.keySet());
    }

    public ItemStack createLootboxItem(Lootbox lootbox) {
        ItemStack item = lootbox.getDisplayItem().clone();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Store lootbox name in persistent data
            meta.getPersistentDataContainer().set(lootboxKey, PersistentDataType.STRING, lootbox.getName());
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public String getLootboxFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        return meta.getPersistentDataContainer().get(lootboxKey, PersistentDataType.STRING);
    }

    public NamespacedKey getLootboxKey() {
        return lootboxKey;
    }
}
