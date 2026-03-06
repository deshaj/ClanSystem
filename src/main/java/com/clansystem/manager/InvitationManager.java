package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanInvitation;
import com.clansystem.data.JoinRequest;
import com.clansystem.database.PlayerRepository;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationManager {
    private final ClanSystem plugin;
    private final PlayerRepository playerRepository;
    
    @Getter
    private final Map<UUID, List<ClanInvitation>> playerInvitations = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<UUID, List<JoinRequest>> clanRequests = new ConcurrentHashMap<>();
    
    public InvitationManager(ClanSystem plugin, PlayerRepository playerRepository) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
        startExpirationTask();
    }

    public InvitationManager(ClanSystem plugin) {
        this(plugin, plugin.getPlayerRepository());
    }

    public void sendInvitation(Clan clan, UUID playerUUID) {
        long now = System.currentTimeMillis();
        ClanInvitation invitation = new ClanInvitation(
            UUID.randomUUID(),
            clan.getId(),
            playerUUID,
            now,
            now + (3 * 24 * 60 * 60 * 1000L)
        );
        
        playerInvitations.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(invitation);
        
        playerRepository.addInvitation(playerUUID, clan.getId());
        
        Player target = Bukkit.getPlayer(playerUUID);
        if (target != null && target.isOnline()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("clan", clan.getName());
            plugin.getMessageManager().send(target, "invite.received", placeholders);
            plugin.getSoundManager().play(target, "invite-receive");
        }
    }

    public void sendJoinRequest(UUID playerUUID, Clan clan, String message) {
        JoinRequest request = new JoinRequest(
            UUID.randomUUID(),
            clan.getId(),
            playerUUID,
            message,
            System.currentTimeMillis(),
            (long)(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L))
        );
        
        clanRequests.computeIfAbsent(clan.getId(), k -> new ArrayList<>()).add(request);
        
        Player requester = Bukkit.getPlayer(playerUUID);
        if (requester != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("clan", clan.getName());
            plugin.getMessageManager().send(requester, "request.sent", placeholders);
            plugin.getSoundManager().play(requester, "invite-send");
        }

        Player owner = Bukkit.getPlayer(clan.getOwner());
        if (owner != null && owner.isOnline()) {
            String playerName = requester != null ? requester.getName() : "Unknown";
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            plugin.getMessageManager().send(owner, "request.received", placeholders);
            plugin.getSoundManager().play(owner, "invite-receive");
        }
    }
    
    public boolean hasInvitation(UUID playerUUID, UUID clanId) {
        List<ClanInvitation> invitations = playerInvitations.get(playerUUID);
        if (invitations == null) return false;
        
        return invitations.stream().anyMatch(inv -> 
            inv.getClanId().equals(clanId) && !inv.isExpired()
        );
    }
    
    public CompletableFuture<Void> removeInvitation(UUID playerUUID, UUID clanId) {
        List<ClanInvitation> invitations = playerInvitations.get(playerUUID);
        if (invitations != null) {
            invitations.removeIf(inv -> inv.getClanId().equals(clanId));
            if (invitations.isEmpty()) {
                playerInvitations.remove(playerUUID);
            }
        }
        return playerRepository.removeInvitation(playerUUID, clanId);
    }
    
    public List<ClanInvitation> getPlayerInvitations(UUID playerUUID) {
        List<ClanInvitation> invitations = playerInvitations.get(playerUUID);
        if (invitations == null) return new ArrayList<>();
        
        invitations.removeIf(ClanInvitation::isExpired);
        return new ArrayList<>(invitations);
    }
    
    public List<JoinRequest> getClanRequests(UUID clanId) {
        List<JoinRequest> requests = clanRequests.get(clanId);
        if (requests == null) return new ArrayList<>();
        
        requests.removeIf(JoinRequest::isExpired);
        return new ArrayList<>(requests);
    }
    
    public void removeRequest(UUID requestId) {
        for (List<JoinRequest> requests : clanRequests.values()) {
            requests.removeIf(req -> req.getId().equals(requestId));
        }
    }
    
    public JoinRequest getRequest(UUID requestId) {
        for (List<JoinRequest> requests : clanRequests.values()) {
            for (JoinRequest request : requests) {
                if (request.getId().equals(requestId)) {
                    return request;
                }
            }
        }
        return null;
    }
    
    public CompletableFuture<Void> loadPlayerInvitations(UUID playerUUID) {
        return playerRepository.getInvitations(playerUUID).thenAccept(clanIds -> {
            List<ClanInvitation> invitations = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (UUID clanId : clanIds) {
                ClanInvitation invitation = new ClanInvitation(
                    UUID.randomUUID(),
                    clanId,
                    playerUUID,
                    now,
                    now + (3L * 24 * 60 * 60 * 1000)
                );
                invitations.add(invitation);
            }
            
            if (!invitations.isEmpty()) {
                playerInvitations.put(playerUUID, invitations);
            }
        });
    }
    
    private void startExpirationTask() {
        plugin.getFoliaLib().getImpl().runTimerAsync(() -> {
            for (List<ClanInvitation> invitations : playerInvitations.values()) {
                invitations.removeIf(ClanInvitation::isExpired);
            }
            
            for (List<JoinRequest> requests : clanRequests.values()) {
                requests.removeIf(JoinRequest::isExpired);
            }
            
            playerInvitations.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            clanRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }, 20L * 60L, 20L * 60L);
    }
}