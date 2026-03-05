package com.clansystem.manager;

import com.clansystem.ClanSystem;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final ClanSystem plugin;
    private FileConfiguration config;
    
    public ConfigManager(ClanSystem plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }

    public boolean debug() {
        return config.getBoolean("debug", false);
    }
    
    public int maxNameLength() {
        return config.getInt("clan.max-name-length", 16);
    }
    
    public int minNameLength() {
        return config.getInt("clan.min-name-length", 3);
    }
    
    public int maxMembers() {
        return config.getInt("clan.max-members", 10);
    }
    
    public int homeCooldown() {
        return config.getInt("clan.home-cooldown", 5);
    }
    
    public boolean levelingEnabled() {
        return config.getBoolean("level.enabled", true);
    }
    
    public int getRequiredKills(int level) {
        return config.getInt("level.levels." + level + ".required-kills", 0);
    }
    
    public String getLevelTag(int level) {
        return config.getString("level.levels." + level + ".tag", "&7[Clan]");
    }
    
    public int getMaxLevel() {
        int maxLevel = 1;
        if (config.contains("level.levels")) {
            for (String key : config.getConfigurationSection("level.levels").getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    if (level > maxLevel) {
                        maxLevel = level;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return maxLevel;
    }
}