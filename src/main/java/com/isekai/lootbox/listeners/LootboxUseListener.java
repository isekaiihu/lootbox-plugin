package com.isekai.lootbox.listeners;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.gui.FullinvClaimGUI;
import com.isekai.lootbox.gui.LootboxOpenGUI;
import com.isekai.lootbox.gui.RewardSelectGUI;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LootboxUseListener implements Listener {

    private final LootboxPlugin plugin;

    public LootboxUseListener(LootboxPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Avoid double-firing for off-hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        ConfigManager config = plugin.getConfigManager();

        if (item == null) {
            return;
        }

        // Check if item is a lootbox
        String lootboxName = plugin.getLootboxManager().getLootboxFromItem(item);

        if (lootboxName == null) {
            return;
        }

        Lootbox lootbox = plugin.getLootboxManager().getLootbox(lootboxName);

        if (lootbox == null) {
            player.sendMessage(config.getMessage("lootbox-no-longer-exists"));
            return;
        }

        if (!player.hasPermission("lootbox.use")) {
            player.sendMessage(config.getMessage("no-use-permission"));
            return;
        }

        event.setCancelled(true);

        // Check if lootbox has rewards
        if (lootbox.getRewards().isEmpty()) {
            player.sendMessage(config.getMessage("no-rewards"));
            return;
        }

        // Check if player has remaining claims for this lootbox
        int remainingClaims = plugin.getPlayerDataManager().getRemainingClaims(player.getUniqueId(), lootboxName);
        
        if (remainingClaims > 0) {
            // Player has remaining claims, open select GUI directly
            // Use one claim
            plugin.getPlayerDataManager().useOneClaim(player.getUniqueId(), lootboxName);
            
            // Don't consume lootbox item for remaining claims
            new RewardSelectGUI(plugin, lootbox).open(player);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("remaining", String.valueOf(remainingClaims - 1));
            
            return;
        }

        // Remove one lootbox from hand
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // Open appropriate GUI based on lootbox type
        if (lootbox.isFullinv()) {
            new FullinvClaimGUI(plugin, lootbox).open(player);
        } else {
            new LootboxOpenGUI(plugin, lootbox).open(player);
        }
    }
}
