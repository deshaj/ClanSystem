package com.clansystem;

import com.clansystem.command.ClanCommand;
import com.clansystem.inventory.gui.GUIListener;
import com.clansystem.inventory.gui.GUIManager;
import com.clansystem.listener.ChatListener;
import com.clansystem.listener.PlayerListener;
import com.clansystem.manager.*;
import com.clansystem.placeholder.ClanPlaceholder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class ClanSystem extends JavaPlugin {
    
    private ConfigManager configManager;
    private MessageManager messageManager;
    private SoundManager soundManager;
    private ClanManager clanManager;
    private PlayerDataManager playerDataManager;
    private ClanChatManager clanChatManager;
    private HomeManager homeManager;
    private LevelManager levelManager;
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.soundManager = new SoundManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.clanManager = new ClanManager(this);
        this.clanChatManager = new ClanChatManager();
        this.homeManager = new HomeManager(this);
        this.levelManager = new LevelManager(this);
        this.guiManager = new GUIManager();
        
        registerListeners();
        registerCommands();
        registerPlaceholders();
        
        getLogger().info("ClanSystem has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (clanManager != null) {
            clanManager.saveAll();
        }
        getLogger().info("ClanSystem has been disabled!");
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
    }
    
    private void registerCommands() {
        ClanCommand clanCommand = new ClanCommand(this);
        getCommand("clan").setExecutor(clanCommand);
        getCommand("clan").setTabCompleter(clanCommand);
        getCommand("cc").setExecutor(clanCommand);
        getCommand("cc").setTabCompleter(clanCommand);
    }
    
    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hooked successfully!");
        }
    }
}