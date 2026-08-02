package com.isekai.lootbox.managers;

import com.isekai.lootbox.LootboxPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player-specific data like remaining lootbox claims
 */
public class PlayerDataManager {

    private final LootboxPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    // playerUUID -> lootboxName -> remaining claims
    private final Map<UUID, Map<String, Integer>> remainingClaims = new HashMap<>();

    public PlayerDataManager(LootboxPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        remainingClaims.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("players");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerSection = section.getConfigurationSection(uuidStr);
                if (playerSection == null) continue;

                Map<String, Integer> playerClaims = new HashMap<>();
                for (String lootboxName : playerSection.getKeys(false)) {
                    int claims = playerSection.getInt(lootboxName, 0);
                    if (claims > 0) {
                        playerClaims.put(lootboxName.toLowerCase(), claims);
                    }
                }

                if (!playerClaims.isEmpty()) {
                    remainingClaims.put(uuid, playerClaims);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidStr);
            }
        }

        plugin.getLogger().info("Loaded player data for " + remainingClaims.size() + " players!");
    }

    public void saveData() {
        dataConfig = new YamlConfiguration();

        for (Map.Entry<UUID, Map<String, Integer>> entry : remainingClaims.entrySet()) {
            UUID uuid = entry.getKey();
            Map<String, Integer> claims = entry.getValue();

            for (Map.Entry<String, Integer> claimEntry : claims.entrySet()) {
                if (claimEntry.getValue() > 0) {
                    dataConfig.set("players." + uuid.toString() + "." + claimEntry.getKey(), claimEntry.getValue());
                }
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Get remaining claims for a player on a specific lootbox
     */
    public int getRemainingClaims(UUID playerId, String lootboxName) {
        Map<String, Integer> playerClaims = remainingClaims.get(playerId);
        if (playerClaims == null) return 0;
        return playerClaims.getOrDefault(lootboxName.toLowerCase(), 0);
    }

    /**
     * Set remaining claims for a player on a specific lootbox
     */
    public void setRemainingClaims(UUID playerId, String lootboxName, int claims) {
        if (claims <= 0) {
            // Remove entry if no claims remaining
            Map<String, Integer> playerClaims = remainingClaims.get(playerId);
            if (playerClaims != null) {
                playerClaims.remove(lootboxName.toLowerCase());
                if (playerClaims.isEmpty()) {
                    remainingClaims.remove(playerId);
                }
            }
        } else {
            remainingClaims.computeIfAbsent(playerId, k -> new HashMap<>())
                    .put(lootboxName.toLowerCase(), claims);
        }
        saveData();
    }

    /**
     * Add remaining claims for a player
     */
    public void addRemainingClaims(UUID playerId, String lootboxName, int claims) {
        int current = getRemainingClaims(playerId, lootboxName);
        setRemainingClaims(playerId, lootboxName, current + claims);
    }

    /**
     * Use one claim and return remaining
     */
    public int useOneClaim(UUID playerId, String lootboxName) {
        int current = getRemainingClaims(playerId, lootboxName);
        if (current > 0) {
            setRemainingClaims(playerId, lootboxName, current - 1);
            return current - 1;
        }
        return 0;
    }

    /**
     * Check if player has remaining claims
     */
    public boolean hasRemainingClaims(UUID playerId, String lootboxName) {
        return getRemainingClaims(playerId, lootboxName) > 0;
    }

    /**
     * Clear all claims for a lootbox (when deleted)
     */
    public void clearLootboxClaims(String lootboxName) {
        String key = lootboxName.toLowerCase();
        for (Map<String, Integer> playerClaims : remainingClaims.values()) {
            playerClaims.remove(key);
        }
        saveData();
    }
}
