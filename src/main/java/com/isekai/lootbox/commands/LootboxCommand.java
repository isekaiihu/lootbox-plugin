package com.isekai.lootbox.commands;

import com.isekai.lootbox.LootboxPlugin;
import com.isekai.lootbox.gui.RewardEditorGUI;
import com.isekai.lootbox.managers.ConfigManager;
import com.isekai.lootbox.models.Lootbox;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LootboxCommand implements CommandExecutor, TabCompleter {

    private final LootboxPlugin plugin;

    public LootboxCommand(LootboxPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;
        ConfigManager config = plugin.getConfigManager();

        if (!player.hasPermission("lootbox.admin")) {
            player.sendMessage(config.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "item":
                handleItem(player, args);
                break;
            case "give":
                handleGive(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "rewards":
                handleRewards(player, args);
                break;
            case "type":
                handleType(player, args);
                break;
            case "limit":
                handleLimit(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        ConfigManager config = plugin.getConfigManager();
        player.sendMessage(config.getMessageRaw("help-header"));
        player.sendMessage(config.getMessageRaw("help-create"));
        player.sendMessage(config.getMessageRaw("help-item"));
        player.sendMessage(config.getMessageRaw("help-give"));
        player.sendMessage(config.getMessageRaw("help-rewards"));
        player.sendMessage(config.getMessageRaw("help-type"));
        player.sendMessage(config.getMessageRaw("help-limit"));
        player.sendMessage(config.getMessageRaw("help-delete"));
        player.sendMessage(config.getMessageRaw("help-list"));
        player.sendMessage(config.getMessageRaw("help-reload"));
    }

    private void handleRewards(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(config.getMessage("help-rewards"));
            return;
        }

        String name = args[1];
        Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);

        if (lootbox == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
            return;
        }

        new RewardEditorGUI(plugin, lootbox).open(player);
    }

    private void handleCreate(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 3) {
            player.sendMessage(config.getMessage("help-create"));
            return;
        }

        String name = args[1];
        int rows;

        try {
            rows = Integer.parseInt(args[2]);
            if (rows < 1 || rows > 6) {
                player.sendMessage(config.getMessage("invalid-rows"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(config.getMessage("invalid-rows"));
            return;
        }

        Lootbox lootbox = plugin.getLootboxManager().createLootbox(name, rows);

        if (lootbox == null) {
            player.sendMessage(config.getMessage("lootbox-already-exists"));
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("rows", String.valueOf(rows));
        player.sendMessage(config.getMessage("lootbox-created", placeholders));

        // Open reward editor immediately
        new RewardEditorGUI(plugin, lootbox).open(player);
    }

    private void handleItem(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(config.getMessage("help-item"));
            return;
        }

        String name = args[1];
        Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);

        if (lootbox == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem == null || heldItem.getType().isAir()) {
            player.sendMessage(config.getMessage("hold-item"));
            return;
        }

        lootbox.setDisplayItem(heldItem);
        plugin.getLootboxManager().saveLootboxes();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        player.sendMessage(config.getMessage("item-set", placeholders));
    }

    private void handleGive(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 3) {
            player.sendMessage(config.getMessage("help-give"));
            return;
        }

        String name = args[1];
        Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);

        if (lootbox == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
            return;
        }

        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", args[2]);
            player.sendMessage(config.getMessage("player-not-found", placeholders));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1 || amount > 64) {
                    player.sendMessage(config.getMessage("invalid-amount"));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(config.getMessage("invalid-amount"));
                return;
            }
        }

        ItemStack lootboxItem = plugin.getLootboxManager().createLootboxItem(lootbox);
        lootboxItem.setAmount(amount);

        target.getInventory().addItem(lootboxItem);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("amount", String.valueOf(amount));
        placeholders.put("player", target.getName());
        
        player.sendMessage(config.getMessage("lootbox-given", placeholders));
        target.sendMessage(config.getMessage("lootbox-received", placeholders));
    }

    private void handleDelete(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(config.getMessage("help-delete"));
            return;
        }

        String name = args[1];
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);

        if (plugin.getLootboxManager().deleteLootbox(name)) {
            player.sendMessage(config.getMessage("lootbox-deleted", placeholders));
        } else {
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
        }
    }

    private void handleType(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 3) {
            player.sendMessage(config.getMessage("help-type"));
            return;
        }

        String name = args[1];
        String type = args[2].toLowerCase();
        
        Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);

        if (lootbox == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
            return;
        }

        if (!type.equals("normal") && !type.equals("fullinv")) {
            player.sendMessage(config.getMessage("invalid-type"));
            return;
        }

        lootbox.setType(type);
        plugin.getLootboxManager().saveLootboxes();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("type", type);
        player.sendMessage(config.getMessage("type-set", placeholders));
    }

    private void handleLimit(Player player, String[] args) {
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length < 3) {
            player.sendMessage(config.getMessage("help-limit"));
            return;
        }

        String name = args[1];
        int limit;
        
        try {
            limit = Integer.parseInt(args[2]);
            if (limit < 1) {
                player.sendMessage(config.getMessage("invalid-limit"));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(config.getMessage("invalid-limit"));
            return;
        }
        
        Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);

        if (lootbox == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            player.sendMessage(config.getMessage("lootbox-not-found", placeholders));
            return;
        }

        lootbox.setRewardLimit(limit);
        plugin.getLootboxManager().saveLootboxes();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("limit", String.valueOf(limit));
        player.sendMessage(config.getMessage("reward-limit-set", placeholders));
    }

    private void handleList(Player player) {
        ConfigManager config = plugin.getConfigManager();
        List<String> names = plugin.getLootboxManager().getLootboxNames();

        if (names.isEmpty()) {
            player.sendMessage(config.getMessage("no-lootboxes"));
            return;
        }

        player.sendMessage(config.getMessageRaw("list-header"));
        for (String name : names) {
            Lootbox lootbox = plugin.getLootboxManager().getLootbox(name);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", name);
            placeholders.put("rewards", String.valueOf(lootbox.getRewards().size()));
            placeholders.put("rows", String.valueOf(lootbox.getRows()));
            placeholders.put("type", lootbox.getTypeName());
            placeholders.put("limit", String.valueOf(lootbox.getRewardLimit()));
            player.sendMessage(config.getMessageRaw("list-entry", placeholders));
        }
    }

    private void handleReload(Player player) {
        plugin.reloadPlugin();
        player.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "item", "give", "rewards", "type", "limit", "delete", "list", "reload"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "item":
                case "give":
                case "rewards":
                case "type":
                case "limit":
                case "delete":
                    completions.addAll(plugin.getLootboxManager().getLootboxNames());
                    break;
                case "create":
                    completions.add("<name>");
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "give":
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                    break;
                case "create":
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6"));
                    break;
                case "type":
                    completions.addAll(Arrays.asList("normal", "fullinv"));
                    break;
                case "limit":
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "10"));
                    break;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give")) {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "10", "16", "32", "64"));
            }
        }

        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
