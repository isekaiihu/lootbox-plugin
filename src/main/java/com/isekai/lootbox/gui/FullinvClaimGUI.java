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
import java.util.List;
import java.util.Map;

/**
 * GUI for fullinv lootbox type - shows claim button to get all rewards
 */
public class FullinvClaimGUI implements InventoryHolder {

    private final LootboxPlugin plugin;
    private final Lootbox lootbox;
    private final Inventory inventory;
    private final int claimSlot;

    public FullinvClaimGUI(LootboxPlugin plugin, Lootbox lootbox) {
        this.plugin = plugin;
        this.lootbox = lootbox;
        
        ConfigManager config = plugin.getConfigManager();
        
        String title = config.getFullinvGuiTitle(lootbox.getName());
        int rows = config.getFullinvGuiRows();
        
        this.inventory = Bukkit.createInventory(this, rows * 9, config.colorizeComponent(title));
        this.claimSlot = config.getFullinvClaimSlot();
        
        setupGUI();
    }

    private void setupGUI() {
        ConfigManager config = plugin.getConfigManager();
        
        // Fill with filler if enabled
        if (config.isFullinvFillerEnabled()) {
            ItemStack filler = config.createItem(
                config.getFullinvFillerMaterial(),
                config.getFullinvFillerName(),
                config.getFullinvFillerLore()
            );
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", lootbox.getName());
        placeholders.put("rewards", String.valueOf(lootbox.getRewards().size()));

        // Claim button
        ItemStack claimButton = config.createItem(
            config.getFullinvClaimMaterial(),
            config.getFullinvClaimName(),
            config.getFullinvClaimLore(),
            placeholders
        );
        inventory.setItem(claimSlot, claimButton);

        // Show preview rewards in configured slots
        List<Integer> previewSlots = config.getFullinvPreviewSlots();
        List<ItemStack> rewards = lootbox.getRewards();
        
        for (int i = 0; i < previewSlots.size() && i < rewards.size(); i++) {
            int slot = previewSlots.get(i);
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, rewards.get(i).clone());
            }
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

    public int getClaimSlot() {
        return claimSlot;
    }
}
