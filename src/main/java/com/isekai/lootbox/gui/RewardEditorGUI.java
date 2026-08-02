package com.isekai.lootbox.gui;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class RewardEditorGUI implements InventoryHolder {

    private final LootboxPlugin plugin;
    private final Lootbox lootbox;
    private final Inventory inventory;

    public RewardEditorGUI(LootboxPlugin plugin, Lootbox lootbox) {
        this.plugin = plugin;
        this.lootbox = lootbox;
        
        ConfigManager config = plugin.getConfigManager();
        String title = config.getEditorGuiTitle(lootbox.getName());
        
        this.inventory = Bukkit.createInventory(this, lootbox.getRows() * 9, config.colorizeComponent(title));
        
        // Load existing rewards
        for (int i = 0; i < lootbox.getRewards().size() && i < inventory.getSize(); i++) {
            inventory.setItem(i, lootbox.getRewards().get(i).clone());
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Lootbox getLootbox() {
        return lootbox;
    }

    public void saveRewards() {
        lootbox.clearRewards();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                lootbox.addReward(item);
            }
        }
        plugin.getLootboxManager().saveLootboxes();
    }
}
