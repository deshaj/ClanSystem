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
        playSound(player, "sounds.success");
    }
    
    public void playError(Player player) {
        playSound(player, "sounds.error");
    }
    
    public void playClick(Player player) {
        playSound(player, "sounds.click");
    }
    
    public void playOpen(Player player) {
        playSound(player, "sounds.open");
    }
    
    public void playJoin(Player player) {
        playSound(player, "sounds.join");
    }
    
    public void playLeave(Player player) {
        playSound(player, "sounds.leave");
    }
    
    public void playKick(Player player) {
        playSound(player, "sounds.kick");
    }
    
    public void playPromote(Player player) {
        playSound(player, "sounds.promote");
    }
    
    public void playDemote(Player player) {
        playSound(player, "sounds.demote");
    }
    
    public void playLevelUp(Player player) {
        playSound(player, "sounds.levelup");
    }
    
    public void playDisband(Player player) {
        playSound(player, "sounds.disband");
    }
    
    public void playTeleport(Player player) {
        playSound(player, "sounds.teleport");
    }
    
    public void play(Player player, String key) {
        playSound(player, "sounds." + key.replace("-", "."));
    }

    public void playSound(Player player, String path) {
        if (!plugin.getConfigManager().getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }
        
        String soundName = plugin.getConfigManager().getConfig().getString(path);
        if (soundName == null || soundName.equalsIgnoreCase("none")) {
            return;
        }
        
        float volume = (float) plugin.getConfigManager().getConfig().getDouble(path + "-volume", 1.0);
        float pitch = (float) plugin.getConfigManager().getConfig().getDouble(path + "-pitch", 1.0);
        
        XSound.matchXSound(soundName).ifPresent(sound -> 
            sound.play(player, volume, pitch)
        );
    }
}