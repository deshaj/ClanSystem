package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.database.PlayerRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final ClanSystem plugin;
    private final PlayerRepository repository;
    private final Map<UUID, UUID> cachedClanIds = new ConcurrentHashMap<>();
    
    public PlayerDataManager(ClanSystem plugin, PlayerRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }
    
    public CompletableFuture<Void> setPlayerClan(UUID playerUUID, UUID clanId) {
        if (clanId == null) {
            cachedClanIds.remove(playerUUID);
        } else {
            cachedClanIds.put(playerUUID, clanId);
        }
        return repository.setPlayerClan(playerUUID, clanId);
    }
    
    public UUID getPlayerClanSync(UUID playerUUID) {
        return cachedClanIds.get(playerUUID);
    }
    
    public CompletableFuture<UUID> getPlayerClan(UUID playerUUID) {
        UUID cached = cachedClanIds.get(playerUUID);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        return repository.getPlayerClan(playerUUID).thenApply(clanId -> {
            if (clanId != null) {
                cachedClanIds.put(playerUUID, clanId);
            }
            return clanId;
        });
    }
    
    public CompletableFuture<Void> addInvitation(UUID playerUUID, UUID clanId) {
        return repository.addInvitation(playerUUID, clanId);
    }
    
    public CompletableFuture<Void> removeInvitation(UUID playerUUID, UUID clanId) {
        return repository.removeInvitation(playerUUID, clanId);
    }
    
    public CompletableFuture<Boolean> hasInvitation(UUID playerUUID, UUID clanId) {
        return repository.hasInvitation(playerUUID, clanId);
    }
    
    public CompletableFuture<Set<UUID>> getInvitations(UUID playerUUID) {
        return repository.getInvitations(playerUUID);
    }
    
    public void cachePlayerClan(UUID playerUUID, UUID clanId) {
        if (clanId != null) {
            cachedClanIds.put(playerUUID, clanId);
        }
    }
    
    public void clearCache(UUID playerUUID) {
        cachedClanIds.remove(playerUUID);
    }
}