package com.clansystem.manager;

import com.clansystem.ClanSystem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final ClanSystem plugin;
    private FileConfiguration messages;
    private final Map<String, String> icons = new HashMap<>();
    
    public MessageManager(ClanSystem plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void reload() {
        loadMessages();
    }
    
    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        
        icons.clear();
        if (messages.contains("icons")) {
            for (String key : messages.getConfigurationSection("icons").getKeys(false)) {
                icons.put("icon_" + key, messages.getString("icons." + key));
            }
        }
    }
    
    public String getMessage(String path) {
        String message = messages.getString(path, "&cMessage not found: " + path);
        return format(message);
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
    
    public void send(CommandSender sender, String path) {
        sender.sendMessage(getPrefix() + getMessage(path));
    }
    
    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getPrefix() + getMessage(path, placeholders));
    }
    
    private String format(String message) {
        for (Map.Entry<String, String> icon : icons.entrySet()) {
            message = message.replace("{" + icon.getKey() + "}", icon.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getPrefix() {
        return format(messages.getString("prefix", "&8[&bClan&8] &7"));
    }
    
    public String getIcon(String name) {
        return format(icons.getOrDefault("icon_" + name, ""));
    }
}