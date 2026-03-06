package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

public class SoundManager {
    private final ClanSystem plugin;
    
    public SoundManager(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    public void playSuccess(Player player) {
        play(player, "success");
    }
    
    public void playError(Player player) {
        play(player, "error");
    }
    
    public void playClick(Player player) {
        play(player, "click");
    }
    
    public void playOpen(Player player) {
        play(player, "open");
    }
    
    public void playJoin(Player player) {
        play(player, "join");
    }
    
    public void playLeave(Player player) {
        play(player, "leave");
    }
    
    public void playKick(Player player) {
        play(player, "kick");
    }
    
    public void playPromote(Player player) {
        play(player, "promote");
    }
    
    public void playDemote(Player player) {
        play(player, "demote");
    }
    
    public void playLevelUp(Player player) {
        play(player, "levelup");
    }
    
    public void playDisband(Player player) {
        play(player, "disband");
    }

    public void playCreate(Player player) {
        play(player, "clan-create");
    }
    
    public void playTeleport(Player player) {
        play(player, "teleport");
    }
    
    public void playSound(Player player, String key) {
        play(player, key);
    }

    public void play(Player player, String key) {
        if (!plugin.getConfigManager().getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }
        
        String soundPath = "sounds." + key;
        String soundName = plugin.getConfigManager().getConfig().getString(soundPath);
        
        if (soundName == null || soundName.equalsIgnoreCase("none")) {
            plugin.debug("Sound not configured: " + key);
            return;
        }
        
        float volume = (float) plugin.getConfigManager().getConfig().getDouble(soundPath + "-volume", 1.0);
        float pitch = (float) plugin.getConfigManager().getConfig().getDouble(soundPath + "-pitch", 1.0);
        
        XSound.matchXSound(soundName).ifPresentOrElse(
            sound -> {
                plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                    sound.play(player, volume, pitch);
                });
                plugin.debug("Played sound '" + soundName + "' for " + player.getName());
            },
            () -> plugin.warn("Invalid sound configured: " + soundName)
        );
    }
}