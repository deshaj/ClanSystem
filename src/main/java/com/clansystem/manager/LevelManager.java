package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import org.bukkit.ChatColor;

public class LevelManager {
    private final ClanSystem plugin;
    
    public LevelManager(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    public int calculateLevel(Clan clan) {
        if (!plugin.getConfigManager().levelingEnabled()) {
            return 1;
        }
        
        int kills = clan.getTotalKills();
        int level = 1;
        int maxLevel = plugin.getConfigManager().getMaxLevel();
        
        for (int i = maxLevel; i >= 1; i--) {
            int required = plugin.getConfigManager().getRequiredKills(i);
            if (kills >= required) {
                level = i;
                break;
            }
        }
        
        return level;
    }
    
    public String getClanTag(Clan clan) {
        int level = calculateLevel(clan);
        String tag = plugin.getConfigManager().getLevelTag(level);
        tag = tag.replace("{clan}", clan.getName());
        return ChatColor.translateAlternateColorCodes('&', tag);
    }
    
    public int getRequiredKills(int level) {
        return plugin.getConfigManager().getRequiredKills(level);
    }
    
    public int getNextLevelKills(Clan clan) {
        int currentLevel = calculateLevel(clan);
        int maxLevel = plugin.getConfigManager().getMaxLevel();
        
        if (currentLevel >= maxLevel) {
            return -1;
        }
        
        return getRequiredKills(currentLevel + 1);
    }
    
    public String getNextLevelTag(Clan clan) {
        int currentLevel = calculateLevel(clan);
        int maxLevel = plugin.getConfigManager().getMaxLevel();
        
        if (currentLevel >= maxLevel) {
            return null;
        }
        
        String tag = plugin.getConfigManager().getLevelTag(currentLevel + 1);
        tag = tag.replace("{clan}", clan.getName());
        return ChatColor.translateAlternateColorCodes('&', tag);
    }
    
    public boolean isMaxLevel(Clan clan) {
        return calculateLevel(clan) >= plugin.getConfigManager().getMaxLevel();
    }
}