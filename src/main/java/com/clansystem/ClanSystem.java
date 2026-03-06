package com.clansystem;

import com.clansystem.command.ClanCommand;
import com.clansystem.database.ClanRepository;
import com.clansystem.database.Database;
import com.clansystem.database.PlayerRepository;
import com.clansystem.database.RequestRepository;
import com.clansystem.inventory.gui.GUIListener;
import com.clansystem.inventory.gui.GUIManager;
import com.clansystem.listener.ChatListener;
import com.clansystem.listener.PlayerListener;
import com.clansystem.manager.*;
import com.clansystem.placeholder.ClanPlaceholder;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class ClanSystem extends JavaPlugin {
    
    private FoliaLib foliaLib;
    private Database database;
    private ClanRepository clanRepository;
    private PlayerRepository playerRepository;
    private RequestRepository requestRepository;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private SoundManager soundManager;
    private ClanManager clanManager;
    private PlayerDataManager playerDataManager;
    private ClanChatManager clanChatManager;
    private HomeManager homeManager;
    private LevelManager levelManager;
    private GUIManager guiManager;
    private InvitationManager invitationManager;
    private ChatInputManager chatInputManager;
    
    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        log("Enabling ClanSystem...");
        
        saveDefaultConfig();
        
        this.foliaLib = new FoliaLib(this);
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.soundManager = new SoundManager(this);
        
        this.database = new Database(this);
        this.database.connect();
        
        this.clanRepository = new ClanRepository(this, database);
        this.playerRepository = new PlayerRepository(this, database);
        
        this.playerDataManager = new PlayerDataManager(this, playerRepository);
        this.clanManager = new ClanManager(this, clanRepository);
        this.clanChatManager = new ClanChatManager();
        this.homeManager = new HomeManager(this);
        this.levelManager = new LevelManager(this);
        this.requestRepository = new RequestRepository(this, database);
        this.guiManager = new GUIManager(this);
        this.invitationManager = new InvitationManager(this, playerRepository);
        
        registerListeners();
        registerCommands();
        registerPlaceholders();
        
        long loadTime = System.currentTimeMillis() - startTime;
        log("ClanSystem enabled in " + loadTime + "ms!");
    }
    
    @Override
    public void onDisable() {
        log("Disabling ClanSystem...");
        
        if (clanManager != null) {
            clanManager.saveAll().join();
        }
        
        if (foliaLib != null) {
            foliaLib.getScheduler().cancelAllTasks();
        }
        
        if (database != null) {
            database.close();
        }
        
        log("ClanSystem disabled!");
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
            log("PlaceholderAPI hooked successfully!");
        }
    }
    
    public void log(String message) {
        getLogger().info(message);
    }
    
    public void debug(String message) {
        if (configManager.debug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
    
    public void warn(String message) {
        getLogger().warning(message);
    }
    
    public void error(String message, Throwable throwable) {
        getLogger().severe(message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}