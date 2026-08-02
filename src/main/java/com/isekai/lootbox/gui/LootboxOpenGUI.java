package com.isekai.lootbox.gui;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LootboxOpenGUI implements InventoryHolder {

    private final LootboxPlugin plugin;
    private final Lootbox lootbox;
    private final Inventory inventory;
    private final int randomSlot;
    private final int selectSlot;

    public LootboxOpenGUI(LootboxPlugin plugin, Lootbox lootbox) {
        this.plugin = plugin;
        this.lootbox = lootbox;
        
        ConfigManager config = plugin.getConfigManager();
        
        String title = config.getOpenGuiTitle(lootbox.getName());
        int rows = config.getOpenGuiRows();
        
        this.inventory = Bukkit.createInventory(this, rows * 9, config.colorizeComponent(title));
        
        this.randomSlot = config.getRandomButtonSlot();
        this.selectSlot = config.getSelectButtonSlot();
        
        setupGUI();
    }

    private void setupGUI() {
        ConfigManager config = plugin.getConfigManager();
        
        // Fill with filler if enabled
        if (config.isOpenGuiFillerEnabled()) {
            ItemStack filler = config.createItem(
                config.getOpenGuiFillerMaterial(),
                config.getOpenGuiFillerName(),
                config.getOpenGuiFillerLore()
            );
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("limit", String.valueOf(lootbox.getRewardLimit()));
        placeholders.put("name", lootbox.getName());
        placeholders.put("rewards", String.valueOf(lootbox.getRewards().size()));

        // Random Reward Button
        ItemStack randomButton = config.createItem(
            config.getRandomButtonMaterial(),
            config.getRandomButtonName(),
            config.getRandomButtonLore(),
            placeholders
        );
        inventory.setItem(randomSlot, randomButton);

        // Select Reward Button
        ItemStack selectButton = config.createItem(
            config.getSelectButtonMaterial(),
            config.getSelectButtonName(),
            config.getSelectButtonLore(),
            placeholders
        );
        inventory.setItem(selectSlot, selectButton);

        // Show a preview of one reward in the configured slot
        if (config.isPreviewEnabled() && !lootbox.getRewards().isEmpty()) {
            ItemStack previewItem = lootbox.getRewards().get(0).clone();
            inventory.setItem(config.getPreviewSlot(), previewItem);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        
        Sound sound = plugin.getConfigManager().getOpenLootboxSound();
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Lootbox getLootbox() {
        return lootbox;
    }

    public int getRandomSlot() {
        return randomSlot;
    }

    public int getSelectSlot() {
        return selectSlot;
    }
}
