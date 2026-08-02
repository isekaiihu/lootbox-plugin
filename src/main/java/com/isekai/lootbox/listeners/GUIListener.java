package com.isekai.lootbox.listeners;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.gui.FullinvClaimGUI;
import com.isekai.lootbox.gui.LootboxOpenGUI;
import com.isekai.lootbox.gui.RewardEditorGUI;
import com.isekai.lootbox.gui.RewardSelectGUI;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {

    private final LootboxPlugin plugin;

    public GUIListener(LootboxPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ConfigManager config = plugin.getConfigManager();

        // Handle Reward Editor GUI
        if (event.getInventory().getHolder() instanceof RewardEditorGUI) {
            // Allow item manipulation in reward editor
            return;
        }

        // Handle Lootbox Open GUI
        if (event.getInventory().getHolder() instanceof LootboxOpenGUI) {
            event.setCancelled(true);
            
            LootboxOpenGUI gui = (LootboxOpenGUI) event.getInventory().getHolder();
            Lootbox lootbox = gui.getLootbox();
            int slot = event.getRawSlot();

            if (slot == gui.getRandomSlot()) {
                // Random reward selected
                player.closeInventory();
                
                int rewardCount = lootbox.getRewardLimit();
                List<ItemStack> rewards = lootbox.getRandomRewards(rewardCount);
                
                if (!rewards.isEmpty()) {
                    boolean itemsDropped = giveItemsToPlayer(player, rewards);
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("amount", String.valueOf(rewards.size()));
                    
                    if (rewards.size() == 1) {
                        player.sendMessage(config.getMessage("random-reward-received"));
                    } else {
                        player.sendMessage(config.getMessage("random-rewards-received", placeholders));
                    }
                    
                    if (itemsDropped) {
                        player.sendMessage(config.getMessage("items-dropped"));
                    }
                    
                    Sound sound = config.getReceiveRewardSound();
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                } else {
                    player.sendMessage(config.getMessage("no-rewards"));
                }
            } else if (slot == gui.getSelectSlot()) {
                // Open select GUI
                if (lootbox.getRewards().isEmpty()) {
                    player.sendMessage(config.getMessage("no-rewards-to-select"));
                    return;
                }
                new RewardSelectGUI(plugin, lootbox).open(player);
            }
            return;
        }

        // Handle Reward Select GUI
        if (event.getInventory().getHolder() instanceof RewardSelectGUI) {
            event.setCancelled(true);
            
            RewardSelectGUI gui = (RewardSelectGUI) event.getInventory().getHolder();
            int slot = event.getRawSlot();

            // Only process clicks within the inventory bounds
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                gui.selectItem(player, slot);
            }
            return;
        }

        // Handle Fullinv Claim GUI
        if (event.getInventory().getHolder() instanceof FullinvClaimGUI) {
            event.setCancelled(true);
            
            FullinvClaimGUI gui = (FullinvClaimGUI) event.getInventory().getHolder();
            Lootbox lootbox = gui.getLootbox();
            int slot = event.getRawSlot();

            if (slot == gui.getClaimSlot()) {
                // Claim all rewards
                player.closeInventory();
                
                List<ItemStack> rewards = lootbox.getAllRewards();
                
                if (!rewards.isEmpty()) {
                    boolean itemsDropped = giveItemsToPlayer(player, rewards);
                    
                    player.sendMessage(config.getMessage("fullinv-claimed"));
                    
                    if (itemsDropped) {
                        player.sendMessage(config.getMessage("items-dropped"));
                    }
                    
                    Sound sound = config.getClaimFullinvSound();
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                } else {
                    player.sendMessage(config.getMessage("no-rewards"));
                }
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        ConfigManager config = plugin.getConfigManager();

        // Handle Reward Editor GUI close - save rewards
        if (event.getInventory().getHolder() instanceof RewardEditorGUI) {
            RewardEditorGUI gui = (RewardEditorGUI) event.getInventory().getHolder();
            gui.saveRewards();
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", gui.getLootbox().getName());
            player.sendMessage(config.getMessage("rewards-saved", placeholders));
            return;
        }

        // Handle Reward Select GUI close - give selected rewards and track remaining
        if (event.getInventory().getHolder() instanceof RewardSelectGUI) {
            RewardSelectGUI gui = (RewardSelectGUI) event.getInventory().getHolder();
            
            // Check if already processed to prevent duplicates
            if (!RewardSelectGUI.hasPendingSelection(player.getUniqueId())) {
                return;
            }
            RewardSelectGUI.removePendingSelection(player.getUniqueId());
            
            if (gui.hasSelection()) {
                List<ItemStack> rewards = gui.getSelectedRewards();
                boolean itemsDropped = giveItemsToPlayer(player, rewards);
                
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(rewards.size()));
                
                if (rewards.size() == 1) {
                    player.sendMessage(config.getMessage("selected-reward-received"));
                } else {
                    player.sendMessage(config.getMessage("selected-rewards-received", placeholders));
                }
                
                if (itemsDropped) {
                    player.sendMessage(config.getMessage("items-dropped"));
                }
                
                Sound sound = config.getReceiveRewardSound();
                if (sound != null) {
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                }
                
                // Check if there are remaining claims
                int remaining = gui.getRemainingSelections();
                if (remaining > 0) {
                    // Store remaining claims
                    plugin.getPlayerDataManager().addRemainingClaims(
                        player.getUniqueId(), 
                        gui.getLootbox().getName(), 
                        remaining
                    );
                    
                    placeholders.put("remaining", String.valueOf(remaining));
                    player.sendMessage(config.getMessage("remaining-claims", placeholders));
                }
            } else {
                player.sendMessage(config.getMessage("no-selection-made"));
                // Return the lootbox item since nothing was selected
                ItemStack lootboxItem = plugin.getLootboxManager().createLootboxItem(gui.getLootbox());
                player.getInventory().addItem(lootboxItem);
            }
            return;
        }
    }

    /**
     * Give items to player, dropping overflow on ground
     * @return true if any items were dropped
     */
    private boolean giveItemsToPlayer(Player player, List<ItemStack> items) {
        boolean itemsDropped = false;
        
        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            
            if (!leftover.isEmpty()) {
                itemsDropped = true;
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
        
        return itemsDropped;
    }
}
