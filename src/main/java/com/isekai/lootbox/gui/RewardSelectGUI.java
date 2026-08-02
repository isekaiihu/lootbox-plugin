package com.isekai.lootbox.gui;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.List;

public class RewardSelectGUI implements InventoryHolder {

    private final LootboxPlugin plugin;
    private final Lootbox lootbox;
    private Inventory inventory;
    private final Map<Integer, ItemStack> originalItems;
    private final Set<Integer> selectedSlots;
    private final int maxSelections;

    // Static map to track pending selections per player
    private static final Map<UUID, RewardSelectGUI> pendingSelections = new HashMap<>();

    public RewardSelectGUI(LootboxPlugin plugin, Lootbox lootbox) {
        this.plugin = plugin;
        this.lootbox = lootbox;
        this.originalItems = new HashMap<>();
        this.selectedSlots = new HashSet<>();
        this.maxSelections = lootbox.getRewardLimit();
        
        createInventory();
        setupRewards();
    }

    private void createInventory() {
        ConfigManager config = plugin.getConfigManager();
        String title = config.getSelectGuiTitle(lootbox.getName(), 0, maxSelections);
        this.inventory = Bukkit.createInventory(this, lootbox.getRows() * 9, config.colorizeComponent(title));
    }

    private void setupRewards() {
        List<ItemStack> rewards = lootbox.getRewards();
        for (int i = 0; i < rewards.size() && i < inventory.getSize(); i++) {
            ItemStack item = rewards.get(i).clone();
            originalItems.put(i, item.clone());
            inventory.setItem(i, item);
        }
    }

    public void open(Player player) {
        pendingSelections.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    public void selectItem(Player player, int slot) {
        ConfigManager config = plugin.getConfigManager();
        
        // Check if this slot has a reward
        if (!originalItems.containsKey(slot)) {
            return;
        }
        
        // If already selected, deselect it
        if (selectedSlots.contains(slot)) {
            selectedSlots.remove(slot);
            inventory.setItem(slot, originalItems.get(slot).clone());
            updateTitle(player);
            
            Sound sound = config.getSelectRewardSound();
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 0.8f);
            }
            return;
        }
        
        // Check if max selections reached
        if (selectedSlots.size() >= maxSelections) {
            player.sendMessage(config.getMessage("max-selections-reached"));
            return;
        }

        // Set new selection
        selectedSlots.add(slot);
        ItemStack original = originalItems.get(slot);
        
        // Create indicator item with original item's name and lore
        Material indicatorMaterial = config.getSelectedIndicatorMaterial();
        if (indicatorMaterial == null) indicatorMaterial = Material.LIME_STAINED_GLASS_PANE;
        
        ItemStack selectedItem = new ItemStack(indicatorMaterial);
        ItemMeta selectedMeta = selectedItem.getItemMeta();
        ItemMeta originalMeta = original.getItemMeta();
        
        if (selectedMeta != null) {
            // Copy the display name (non-italic)
            if (originalMeta != null && originalMeta.hasDisplayName()) {
                selectedMeta.displayName(originalMeta.displayName()
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                // Use item type name if no custom name
                String itemName = formatMaterialName(original.getType());
                selectedMeta.displayName(Component.text(itemName)
                        .decoration(TextDecoration.ITALIC, false));
            }
            
            // Copy the lore (non-italic)
            if (originalMeta != null && originalMeta.hasLore()) {
                List<Component> nonItalicLore = new ArrayList<>();
                for (Component loreLine : originalMeta.lore()) {
                    nonItalicLore.add(loreLine.decoration(TextDecoration.ITALIC, false));
                }
                selectedMeta.lore(nonItalicLore);
            }
            
            selectedItem.setItemMeta(selectedMeta);
        }
        
        inventory.setItem(slot, selectedItem);
        updateTitle(player);
        
        Sound sound = config.getSelectRewardSound();
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
        }
        
        player.sendMessage(config.getMessage("reward-selected"));
    }

    private void updateTitle(Player player) {
        ConfigManager config = plugin.getConfigManager();
        String title = config.getSelectGuiTitle(lootbox.getName(), selectedSlots.size(), maxSelections);
        
        // Recreate inventory with new title
        Inventory newInventory = Bukkit.createInventory(this, lootbox.getRows() * 9, config.colorizeComponent(title));
        newInventory.setContents(inventory.getContents());
        this.inventory = newInventory;
        
        // Reopen for player
        player.openInventory(newInventory);
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    public List<ItemStack> getSelectedRewards() {
        List<ItemStack> rewards = new ArrayList<>();
        for (int slot : selectedSlots) {
            if (originalItems.containsKey(slot)) {
                rewards.add(originalItems.get(slot).clone());
            }
        }
        return rewards;
    }

    public boolean hasSelection() {
        return !selectedSlots.isEmpty();
    }

    public int getSelectionCount() {
        return selectedSlots.size();
    }

    public int getMaxSelections() {
        return maxSelections;
    }

    public int getRemainingSelections() {
        return maxSelections - selectedSlots.size();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Lootbox getLootbox() {
        return lootbox;
    }

    public static RewardSelectGUI getPendingSelection(UUID playerId) {
        return pendingSelections.get(playerId);
    }

    public static boolean hasPendingSelection(UUID playerId) {
        return pendingSelections.containsKey(playerId);
    }

    public static void removePendingSelection(UUID playerId) {
        pendingSelections.remove(playerId);
    }
}
