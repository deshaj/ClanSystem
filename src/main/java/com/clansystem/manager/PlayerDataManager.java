package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.storage.PlayerDataStorage;

import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {
    private final ClanSystem plugin;
    private final PlayerDataStorage storage;
    
    public PlayerDataManager(ClanSystem plugin) {
        this.plugin = plugin;
        this.storage = new PlayerDataStorage(plugin);
    }
    
    public void setPlayerClan(UUID playerUUID, UUID clanId) {
        storage.setPlayerClan(playerUUID, clanId);
    }
    
    public UUID getPlayerClan(UUID playerUUID) {
        return storage.getPlayerClan(playerUUID);
    }
    
    public void addInvitation(UUID playerUUID, UUID clanId) {
        storage.addInvitation(playerUUID, clanId);
    }
    
    public void removeInvitation(UUID playerUUID, UUID clanId) {
        storage.removeInvitation(playerUUID, clanId);
    }
    
    public boolean hasInvitation(UUID playerUUID, UUID clanId) {
        return storage.hasInvitation(playerUUID, clanId);
    }
    
    public Set<UUID> getInvitations(UUID playerUUID) {
        return storage.getInvitations(playerUUID);
    }
}