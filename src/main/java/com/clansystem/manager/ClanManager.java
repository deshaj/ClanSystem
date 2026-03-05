package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
import com.clansystem.storage.ClansDataStorage;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ClanManager {
    private final ClanSystem plugin;
    private final ClansDataStorage storage;
    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<String, UUID> clansByName = new HashMap<>();
    
    public ClanManager(ClanSystem plugin) {
        this.plugin = plugin;
        this.storage = new ClansDataStorage(plugin);
        loadClans();
    }
    
    private void loadClans() {
        clans.clear();
        clansByName.clear();
        Map<UUID, Clan> loaded = storage.loadAllClans();
        clans.putAll(loaded);
        for (Clan clan : loaded.values()) {
            clansByName.put(clan.getName().toLowerCase(), clan.getId());
        }
    }
    
    public Clan createClan(Player owner, String name) {
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
        storage.saveClan(clan);
        
        return clan;
    }
    
    public void disbandClan(UUID clanId) {
        Clan clan = clans.get(clanId);
        if (clan != null) {
            clansByName.remove(clan.getName().toLowerCase());
            clans.remove(clanId);
            storage.deleteClan(clanId);
            
            for (ClanMember member : clan.getMembers()) {
                plugin.getPlayerDataManager().setPlayerClan(member.getPlayerUUID(), null);
            }
        }
    }
    
    public Clan getClan(UUID id) {
        return clans.get(id);
    }
    
    public Clan getClanByName(String name) {
        UUID id = clansByName.get(name.toLowerCase());
        return id != null ? clans.get(id) : null;
    }
    
    public Clan getPlayerClan(UUID playerUUID) {
        UUID clanId = plugin.getPlayerDataManager().getPlayerClan(playerUUID);
        return clanId != null ? clans.get(clanId) : null;
    }
    
    public boolean clanExists(String name) {
        return clansByName.containsKey(name.toLowerCase());
    }
    
    public void addMember(Clan clan, Player player, ClanRank rank) {
        ClanMember member = new ClanMember(
            player.getUniqueId(),
            player.getName(),
            rank,
            0, 0, 0,
            System.currentTimeMillis()
        );
        clan.getMembers().add(member);
        plugin.getPlayerDataManager().setPlayerClan(player.getUniqueId(), clan.getId());
        storage.saveClan(clan);
    }
    
    public void removeMember(Clan clan, UUID playerUUID) {
        clan.removeMember(playerUUID);
        plugin.getPlayerDataManager().setPlayerClan(playerUUID, null);
        storage.saveClan(clan);
    }
    
    public void updateClan(Clan clan) {
        storage.saveClan(clan);
    }
    
    public Collection<Clan> getAllClans() {
        return clans.values();
    }
    
    public List<Clan> getClansSorted() {
        return clans.values().stream()
            .sorted((c1, c2) -> Integer.compare(c2.getTotalKills(), c1.getTotalKills()))
            .collect(Collectors.toList());
    }
    
    public void promoteMember(Clan clan, UUID playerUUID) {
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerUUID().equals(playerUUID)) {
                if (member.getRank() == ClanRank.MEMBER) {
                    member.setRank(ClanRank.MOD);
                }
                break;
            }
        }
        storage.saveClan(clan);
    }

    public void demoteMember(Clan clan, UUID playerUUID) {
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerUUID().equals(playerUUID)) {
                if (member.getRank() == ClanRank.MOD) {
                    member.setRank(ClanRank.MEMBER);
                }
                break;
            }
        }
        storage.saveClan(clan);
    }

    public void saveAll() {
        for (Clan clan : clans.values()) {
            storage.saveClan(clan);
        }
    }
}