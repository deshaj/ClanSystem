package com.clansystem.storage;

import com.clansystem.ClanSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class DataStorage {
    protected final ClanSystem plugin;
    protected final File file;
    protected FileConfiguration data;
    
    public DataStorage(ClanSystem plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
        
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        this.data = YamlConfiguration.loadConfiguration(file);
    }
    
    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void reload() {
        this.data = YamlConfiguration.loadConfiguration(file);
    }
}