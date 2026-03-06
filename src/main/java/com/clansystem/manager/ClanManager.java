package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
import com.clansystem.database.ClanRepository;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanManager {
    private final ClanSystem plugin;
    private final ClanRepository repository;
    private final Map<UUID, Clan> clans = new ConcurrentHashMap<>();
    private final Map<String, UUID> clansByName = new ConcurrentHashMap<>();
    
    public ClanManager(ClanSystem plugin, ClanRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        loadClans();
    }
    
    private void loadClans() {
        plugin.log("Loading clans from database...");
        repository.loadAllClans().thenAccept(loadedClans -> {
            clans.clear();
            clansByName.clear();
            clans.putAll(loadedClans);
            for (Clan clan : loadedClans.values()) {
                clansByName.put(clan.getName().toLowerCase(), clan.getId());
            }
            plugin.log("Loaded " + clans.size() + " clans");
        }).exceptionally(ex -> {
            plugin.error("Failed to load clans", ex);
            return null;
        });
    }
    
    public CompletableFuture<Clan> createClan(Player owner, String name) {
        UUID id = UUID.randomUUID();
        Clan clan = new Clan();
        clan.setId(id);
        clan.setName(name);
        clan.setOwner(owner.getUniqueId());
        clan.setCreatedAt(System.currentTimeMillis());
        
        ClanMember ownerMember = new ClanMember(
            owner.getUniqueId(),
            owner.getName(),
            ClanRank.OWNER,
            0, 0, 0,
            System.currentTimeMillis()
        );
        clan.getMembers().add(ownerMember);
        
        clans.put(id, clan);
        clansByName.put(name.toLowerCase(), id);
        
        plugin.debug("Created clan: " + name + " (Owner: " + owner.getName() + ")");
        
        return repository.saveClan(clan).thenApply(v -> clan);
    }
    
    public CompletableFuture<Void> disbandClan(UUID clanId) {
        Clan clan = clans.get(clanId);
        if (clan == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        clansByName.remove(clan.getName().toLowerCase());
        clans.remove(clanId);
        
        plugin.debug("Disbanded clan: " + clan.getName());
        
        for (ClanMember member : clan.getMembers()) {
            plugin.getPlayerDataManager().setPlayerClan(member.getPlayerUUID(), null);
        }
        
        return repository.deleteClan(clanId);
    }
    
    public Clan getClan(UUID id) {
        return clans.get(id);
    }
    
    public Clan getClanByName(String name) {
        UUID id = clansByName.get(name.toLowerCase());
        return id != null ? clans.get(id) : null;
    }
    
    public Clan getPlayerClan(UUID playerUUID) {
        UUID clanId = plugin.getPlayerDataManager().getPlayerClanSync(playerUUID);
        return clanId != null ? clans.get(clanId) : null;
    }
    
    public boolean clanExists(String name) {
        return clansByName.containsKey(name.toLowerCase());
    }
    
    public CompletableFuture<Void> addMember(Clan clan, Player player, ClanRank rank) {
        ClanMember member = new ClanMember(
            player.getUniqueId(),
            player.getName(),
            rank,
            0, 0, 0,
            System.currentTimeMillis()
        );
        clan.getMembers().add(member);
        
        plugin.debug("Added member " + player.getName() + " to clan " + clan.getName());
        
        return plugin.getPlayerDataManager().setPlayerClan(player.getUniqueId(), clan.getId())
            .thenCompose(v -> repository.saveClan(clan));
    }
    
    public CompletableFuture<Void> removeMember(Clan clan, UUID playerUUID) {
        clan.removeMember(playerUUID);
        
        plugin.debug("Removed member " + playerUUID + " from clan " + clan.getName());
        
        return plugin.getPlayerDataManager().setPlayerClan(playerUUID, null)
            .thenCompose(v -> repository.saveClan(clan));
    }
    
    public CompletableFuture<Void> updateClan(Clan clan) {
        plugin.debug("Updating clan: " + clan.getName());
        return repository.saveClan(clan);
    }
    
    public Collection<Clan> getAllClans() {
        return clans.values();
    }
    
    public List<Clan> getClansSorted() {
        return clans.values().stream()
            .sorted((c1, c2) -> Integer.compare(c2.getTotalKills(), c1.getTotalKills()))
            .collect(Collectors.toList());
    }
    
    public CompletableFuture<Void> promoteMember(Clan clan, UUID playerUUID) {
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerUUID().equals(playerUUID)) {
                if (member.getRank() == ClanRank.MEMBER) {
                    member.setRank(ClanRank.MOD);
                    plugin.debug("Promoted member " + playerUUID + " in clan " + clan.getName());
                }
                break;
            }
        }
        return repository.saveClan(clan);
    }

    public CompletableFuture<Void> demoteMember(Clan clan, UUID playerUUID) {
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerUUID().equals(playerUUID)) {
                if (member.getRank() == ClanRank.MOD) {
                    member.setRank(ClanRank.MEMBER);
                    plugin.debug("Demoted member " + playerUUID + " in clan " + clan.getName());
                }
                break;
            }
        }
        return repository.saveClan(clan);
    }

    public CompletableFuture<Void> saveAll() {
        plugin.log("Saving all clans...");
        List<CompletableFuture<Void>> futures = clans.values().stream()
            .map(repository::saveClan)
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> plugin.log("All clans saved successfully"));
    }
}