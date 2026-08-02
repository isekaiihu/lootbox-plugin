package com.isekai.lootbox.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lootbox {

    public enum LootboxType {
        NORMAL,     // Random or select rewards
        FULLINV     // Get all rewards at once like a kit
    }

    private String name;
    private int rows;
    private List<ItemStack> rewards;
    private ItemStack displayItem;
    private LootboxType type;
    private int rewardLimit;

    public Lootbox(String name, int rows) {
        this.name = name;
        this.rows = rows;
        this.rewards = new ArrayList<>();
        this.displayItem = new ItemStack(Material.CHEST);
        this.type = LootboxType.NORMAL;
        this.rewardLimit = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }

    public void setRewards(List<ItemStack> rewards) {
        this.rewards = rewards;
    }

    public void addReward(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            rewards.add(item.clone());
        }
    }

    public void clearRewards() {
        rewards.clear();
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem.clone();
    }

    public LootboxType getType() {
        return type;
    }

    public void setType(LootboxType type) {
        this.type = type;
    }

    public void setType(String typeName) {
        try {
            this.type = LootboxType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.type = LootboxType.NORMAL;
        }
    }

    public int getRewardLimit() {
        return rewardLimit;
    }

    public void setRewardLimit(int rewardLimit) {
        this.rewardLimit = Math.max(1, rewardLimit);
    }

    /**
     * Get a single random reward
     */
    public ItemStack getRandomReward() {
        if (rewards.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random() * rewards.size());
        return rewards.get(index).clone();
    }

    /**
     * Get multiple random rewards (up to limit)
     */
    public List<ItemStack> getRandomRewards(int count) {
        if (rewards.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ItemStack> result = new ArrayList<>();
        List<ItemStack> availableRewards = new ArrayList<>(rewards);
        
        for (int i = 0; i < count && !availableRewards.isEmpty(); i++) {
            int index = (int) (Math.random() * availableRewards.size());
            result.add(availableRewards.get(index).clone());
            // Allow duplicates, so don't remove from available
        }
        
        return result;
    }

    /**
     * Get all rewards (for fullinv type)
     */
    public List<ItemStack> getAllRewards() {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack reward : rewards) {
            result.add(reward.clone());
        }
        return result;
    }

    public boolean isNormal() {
        return type == LootboxType.NORMAL;
    }

    public boolean isFullinv() {
        return type == LootboxType.FULLINV;
    }

    public String getTypeName() {
        return type.name().toLowerCase();
    }
}
