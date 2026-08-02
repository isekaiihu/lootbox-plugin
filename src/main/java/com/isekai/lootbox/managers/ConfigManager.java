package com.isekai.lootbox.managers;

import com.isekai.lootbox.LootboxPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final LootboxPlugin plugin;
    private FileConfiguration config;
    
    // Cached messages
    private final Map<String, String> messages = new HashMap<>();
    
    // Cached GUI settings
    private int openGuiRows;
    private boolean openGuiFillerEnabled;
    private Material openGuiFillerMaterial;
    private String openGuiFillerName;
    private List<String> openGuiFillerLore;
    
    private int randomButtonSlot;
    private Material randomButtonMaterial;
    private String randomButtonName;
    private List<String> randomButtonLore;
    
    private int selectButtonSlot;
    private Material selectButtonMaterial;
    private String selectButtonName;
    private List<String> selectButtonLore;
    
    private boolean previewEnabled;
    private int previewSlot;
    
    private Material selectedIndicatorMaterial;
    
    private boolean confirmButtonEnabled;
    private int confirmButtonSlot;
    private Material confirmButtonMaterial;
    private String confirmButtonName;
    private List<String> confirmButtonLore;
    
    private int fullinvGuiRows;
    private boolean fullinvFillerEnabled;
    private Material fullinvFillerMaterial;
    private String fullinvFillerName;
    private List<String> fullinvFillerLore;
    
    private int fullinvClaimSlot;
    private Material fullinvClaimMaterial;
    private String fullinvClaimName;
    private List<String> fullinvClaimLore;
    private List<Integer> fullinvPreviewSlots;
    
    // Sounds
    private Sound openLootboxSound;
    private Sound selectRewardSound;
    private Sound receiveRewardSound;
    private Sound claimFullinvSound;
    
    // Defaults
    private String defaultType;
    private int defaultRewardLimit;

    public ConfigManager(LootboxPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        loadMessages();
        loadGuiSettings();
        loadSounds();
        loadDefaults();
    }

    private void loadMessages() {
        messages.clear();
        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) return;
        
        for (String key : section.getKeys(false)) {
            messages.put(key, section.getString(key, ""));
        }
    }

    private void loadGuiSettings() {
        // Open GUI
        ConfigurationSection openGui = config.getConfigurationSection("guis.open-gui");
        if (openGui != null) {
            openGuiRows = openGui.getInt("rows", 3);
            
            ConfigurationSection filler = openGui.getConfigurationSection("filler");
            if (filler != null) {
                openGuiFillerEnabled = filler.getBoolean("enabled", true);
                openGuiFillerMaterial = Material.matchMaterial(filler.getString("material", "GRAY_STAINED_GLASS_PANE"));
                openGuiFillerName = filler.getString("name", " ");
                openGuiFillerLore = filler.getStringList("lore");
            }
            
            ConfigurationSection random = openGui.getConfigurationSection("random-button");
            if (random != null) {
                randomButtonSlot = random.getInt("slot", 11);
                randomButtonMaterial = Material.matchMaterial(random.getString("material", "LIME_STAINED_GLASS_PANE"));
                randomButtonName = random.getString("name", "&a&lRANDOM &fReward");
                randomButtonLore = random.getStringList("lore");
            }
            
            ConfigurationSection select = openGui.getConfigurationSection("select-button");
            if (select != null) {
                selectButtonSlot = select.getInt("slot", 15);
                selectButtonMaterial = Material.matchMaterial(select.getString("material", "LIGHT_BLUE_STAINED_GLASS_PANE"));
                selectButtonName = select.getString("name", "&b&lSELECT &fReward");
                selectButtonLore = select.getStringList("lore");
            }
            
            ConfigurationSection preview = openGui.getConfigurationSection("preview");
            if (preview != null) {
                previewEnabled = preview.getBoolean("enabled", true);
                previewSlot = preview.getInt("slot", 13);
            }
        }
        
        // Select GUI
        ConfigurationSection selectGui = config.getConfigurationSection("guis.select-gui");
        if (selectGui != null) {
            ConfigurationSection indicator = selectGui.getConfigurationSection("selected-indicator");
            if (indicator != null) {
                selectedIndicatorMaterial = Material.matchMaterial(indicator.getString("material", "LIME_STAINED_GLASS_PANE"));
            }
            
            ConfigurationSection confirm = selectGui.getConfigurationSection("confirm-button");
            if (confirm != null) {
                confirmButtonEnabled = confirm.getBoolean("enabled", true);
                confirmButtonSlot = confirm.getInt("slot", -1);
                confirmButtonMaterial = Material.matchMaterial(confirm.getString("material", "EMERALD_BLOCK"));
                confirmButtonName = confirm.getString("name", "&a&lCONFIRM SELECTION");
                confirmButtonLore = confirm.getStringList("lore");
            }
        }
        
        // Fullinv GUI
        ConfigurationSection fullinvGui = config.getConfigurationSection("guis.fullinv-gui");
        if (fullinvGui != null) {
            fullinvGuiRows = fullinvGui.getInt("rows", 3);
            
            ConfigurationSection filler = fullinvGui.getConfigurationSection("filler");
            if (filler != null) {
                fullinvFillerEnabled = filler.getBoolean("enabled", true);
                fullinvFillerMaterial = Material.matchMaterial(filler.getString("material", "GRAY_STAINED_GLASS_PANE"));
                fullinvFillerName = filler.getString("name", " ");
                fullinvFillerLore = filler.getStringList("lore");
            }
            
            ConfigurationSection claim = fullinvGui.getConfigurationSection("claim-button");
            if (claim != null) {
                fullinvClaimSlot = claim.getInt("slot", 13);
                fullinvClaimMaterial = Material.matchMaterial(claim.getString("material", "EMERALD_BLOCK"));
                fullinvClaimName = claim.getString("name", "&a&lCLAIM ALL REWARDS");
                fullinvClaimLore = claim.getStringList("lore");
            }
            
            fullinvPreviewSlots = fullinvGui.getIntegerList("preview-slots");
        }
    }

    private void loadSounds() {
        String openSound = config.getString("sounds.open-lootbox", "BLOCK_CHEST_OPEN");
        String selectSound = config.getString("sounds.select-reward", "UI_BUTTON_CLICK");
        String receiveSound = config.getString("sounds.receive-reward", "ENTITY_PLAYER_LEVELUP");
        String claimSound = config.getString("sounds.claim-fullinv", "ENTITY_PLAYER_LEVELUP");
        
        try {
            openLootboxSound = openSound.isEmpty() ? null : Sound.valueOf(openSound);
        } catch (Exception e) {
            openLootboxSound = null;
        }
        
        try {
            selectRewardSound = selectSound.isEmpty() ? null : Sound.valueOf(selectSound);
        } catch (Exception e) {
            selectRewardSound = null;
        }
        
        try {
            receiveRewardSound = receiveSound.isEmpty() ? null : Sound.valueOf(receiveSound);
        } catch (Exception e) {
            receiveRewardSound = null;
        }
        
        try {
            claimFullinvSound = claimSound.isEmpty() ? null : Sound.valueOf(claimSound);
        } catch (Exception e) {
            claimFullinvSound = null;
        }
    }

    private void loadDefaults() {
        defaultType = config.getString("defaults.type", "normal");
        defaultRewardLimit = config.getInt("defaults.reward-limit", 1);
    }

    // Message helpers
    public String getMessage(String key) {
        String prefix = messages.getOrDefault("prefix", "");
        String message = messages.getOrDefault(key, "&cMissing message: " + key);
        return colorize(prefix + message);
    }

    public String getMessageRaw(String key) {
        return colorize(messages.getOrDefault(key, "&cMissing message: " + key));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String prefix = messages.getOrDefault("prefix", "");
        String message = messages.getOrDefault(key, "&cMissing message: " + key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return colorize(prefix + message);
    }

    public String getMessageRaw(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "&cMissing message: " + key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return colorize(message);
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }

    public Component colorizeComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(colorize(text))
                .decoration(TextDecoration.ITALIC, false);
    }

    // GUI Title helpers
    public String getOpenGuiTitle(String lootboxName) {
        String title = config.getString("guis.open-gui.title", "&5Lootbox: &6%name%");
        return colorize(title.replace("%name%", lootboxName));
    }

    public String getSelectGuiTitle(String lootboxName, int selected, int limit) {
        String title = config.getString("guis.select-gui.title", "&5Select Reward: &6%name% &7(%selected%/%limit%)");
        return colorize(title.replace("%name%", lootboxName)
                .replace("%selected%", String.valueOf(selected))
                .replace("%limit%", String.valueOf(limit)));
    }

    public String getEditorGuiTitle(String lootboxName) {
        String title = config.getString("guis.editor-gui.title", "&5Edit Rewards: &6%name%");
        return colorize(title.replace("%name%", lootboxName));
    }

    public String getFullinvGuiTitle(String lootboxName) {
        String title = config.getString("guis.fullinv-gui.title", "&5Claim Rewards: &6%name%");
        return colorize(title.replace("%name%", lootboxName));
    }

    // Item creation helpers
    public ItemStack createItem(Material material, String name, List<String> lore) {
        if (material == null) material = Material.STONE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(colorizeComponent(name).decoration(TextDecoration.ITALIC, false));
            
            if (lore != null && !lore.isEmpty()) {
                List<Component> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(colorizeComponent(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public ItemStack createItem(Material material, String name, List<String> lore, Map<String, String> placeholders) {
        List<String> processedLore = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                String processed = line;
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    processed = processed.replace("%" + entry.getKey() + "%", entry.getValue());
                }
                processedLore.add(processed);
            }
        }
        
        String processedName = name;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processedName = processedName.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return createItem(material, processedName, processedLore);
    }

    // Getters for GUI settings
    public int getOpenGuiRows() { return openGuiRows; }
    public boolean isOpenGuiFillerEnabled() { return openGuiFillerEnabled; }
    public Material getOpenGuiFillerMaterial() { return openGuiFillerMaterial; }
    public String getOpenGuiFillerName() { return openGuiFillerName; }
    public List<String> getOpenGuiFillerLore() { return openGuiFillerLore; }
    
    public int getRandomButtonSlot() { return randomButtonSlot; }
    public Material getRandomButtonMaterial() { return randomButtonMaterial; }
    public String getRandomButtonName() { return randomButtonName; }
    public List<String> getRandomButtonLore() { return randomButtonLore; }
    
    public int getSelectButtonSlot() { return selectButtonSlot; }
    public Material getSelectButtonMaterial() { return selectButtonMaterial; }
    public String getSelectButtonName() { return selectButtonName; }
    public List<String> getSelectButtonLore() { return selectButtonLore; }
    
    public boolean isPreviewEnabled() { return previewEnabled; }
    public int getPreviewSlot() { return previewSlot; }
    
    public Material getSelectedIndicatorMaterial() { return selectedIndicatorMaterial; }
    
    public boolean isConfirmButtonEnabled() { return confirmButtonEnabled; }
    public int getConfirmButtonSlot() { return confirmButtonSlot; }
    public Material getConfirmButtonMaterial() { return confirmButtonMaterial; }
    public String getConfirmButtonName() { return confirmButtonName; }
    public List<String> getConfirmButtonLore() { return confirmButtonLore; }
    
    public int getFullinvGuiRows() { return fullinvGuiRows; }
    public boolean isFullinvFillerEnabled() { return fullinvFillerEnabled; }
    public Material getFullinvFillerMaterial() { return fullinvFillerMaterial; }
    public String getFullinvFillerName() { return fullinvFillerName; }
    public List<String> getFullinvFillerLore() { return fullinvFillerLore; }
    
    public int getFullinvClaimSlot() { return fullinvClaimSlot; }
    public Material getFullinvClaimMaterial() { return fullinvClaimMaterial; }
    public String getFullinvClaimName() { return fullinvClaimName; }
    public List<String> getFullinvClaimLore() { return fullinvClaimLore; }
    public List<Integer> getFullinvPreviewSlots() { return fullinvPreviewSlots; }
    
    // Sound getters
    public Sound getOpenLootboxSound() { return openLootboxSound; }
    public Sound getSelectRewardSound() { return selectRewardSound; }
    public Sound getReceiveRewardSound() { return receiveRewardSound; }
    public Sound getClaimFullinvSound() { return claimFullinvSound; }
    
    // Default getters
    public String getDefaultType() { return defaultType; }
    public int getDefaultRewardLimit() { return defaultRewardLimit; }
}
