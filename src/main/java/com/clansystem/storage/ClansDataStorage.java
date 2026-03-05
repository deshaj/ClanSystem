package com.clansystem.storage;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanHome;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class ClansDataStorage extends DataStorage {
    
    public ClansDataStorage(ClanSystem plugin) {
        super(plugin, "clans.yml");
    }
    
    public void saveClan(Clan clan) {
        String path = "clans." + clan.getId().toString();
        data.set(path + ".name", clan.getName());
        data.set(path + ".owner", clan.getOwner().toString());
        data.set(path + ".total-kills", clan.getTotalKills());
        data.set(path + ".total-deaths", clan.getTotalDeaths());
        data.set(path + ".total-playtime", clan.getTotalPlaytime());
        data.set(path + ".total-money", clan.getTotalMoney());
        data.set(path + ".created-at", clan.getCreatedAt());
        
        if (clan.getHome() != null) {
            ClanHome home = clan.getHome();
            data.set(path + ".home.world", home.getWorld());
            data.set(path + ".home.x", home.getX());
            data.set(path + ".home.y", home.getY());
            data.set(path + ".home.z", home.getZ());
            data.set(path + ".home.yaw", home.getYaw());
            data.set(path + ".home.pitch", home.getPitch());
        } else {
            data.set(path + ".home", null);
        }
        
        data.set(path + ".members", null);
        for (int i = 0; i < clan.getMembers().size(); i++) {
            ClanMember member = clan.getMembers().get(i);
            String memberPath = path + ".members." + i;
            data.set(memberPath + ".uuid", member.getPlayerUUID().toString());
            data.set(memberPath + ".name", member.getPlayerName());
            data.set(memberPath + ".rank", member.getRank().name());
            data.set(memberPath + ".kills", member.getKills());
            data.set(memberPath + ".deaths", member.getDeaths());
            data.set(memberPath + ".playtime", member.getPlaytime());
            data.set(memberPath + ".joined-at", member.getJoinedAt());
        }
        
        save();
    }
    
    public void deleteClan(UUID clanId) {
        data.set("clans." + clanId.toString(), null);
        save();
    }
    
    public Map<UUID, Clan> loadAllClans() {
        Map<UUID, Clan> clans = new HashMap<>();
        
        if (!data.contains("clans")) {
            return clans;
        }
        
        ConfigurationSection clansSection = data.getConfigurationSection("clans");
        for (String idString : clansSection.getKeys(false)) {
            try {
                UUID id = UUID.fromString(idString);
                Clan clan = loadClan(id);
                if (clan != null) {
                    clans.put(id, clan);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid clan UUID: " + idString);
            }
        }
        
        return clans;
    }
    
    private Clan loadClan(UUID id) {
        String path = "clans." + id.toString();
        if (!data.contains(path)) {
            return null;
        }
        
        Clan clan = new Clan();
        clan.setId(id);
        clan.setName(data.getString(path + ".name"));
        clan.setOwner(UUID.fromString(data.getString(path + ".owner")));
        clan.setTotalKills(data.getInt(path + ".total-kills", 0));
        clan.setTotalDeaths(data.getInt(path + ".total-deaths", 0));
        clan.setTotalPlaytime(data.getLong(path + ".total-playtime", 0));
        clan.setTotalMoney(data.getDouble(path + ".total-money", 0.0));
        clan.setCreatedAt(data.getLong(path + ".created-at", System.currentTimeMillis()));
        
        if (data.contains(path + ".home")) {
            ClanHome home = new ClanHome(
                data.getString(path + ".home.world"),
                data.getDouble(path + ".home.x"),
                data.getDouble(path + ".home.y"),
                data.getDouble(path + ".home.z"),
                (float) data.getDouble(path + ".home.yaw"),
                (float) data.getDouble(path + ".home.pitch")
            );
            clan.setHome(home);
        }
        
        List<ClanMember> members = new ArrayList<>();
        if (data.contains(path + ".members")) {
            ConfigurationSection membersSection = data.getConfigurationSection(path + ".members");
            for (String index : membersSection.getKeys(false)) {
                String memberPath = path + ".members." + index;
                ClanMember member = new ClanMember(
                    UUID.fromString(data.getString(memberPath + ".uuid")),
                    data.getString(memberPath + ".name"),
                    ClanRank.valueOf(data.getString(memberPath + ".rank")),
                    data.getInt(memberPath + ".kills", 0),
                    data.getInt(memberPath + ".deaths", 0),
                    data.getLong(memberPath + ".playtime", 0),
                    data.getLong(memberPath + ".joined-at", System.currentTimeMillis())
                );
                members.add(member);
            }
        }
        clan.setMembers(members);
        
        return clan;
    }
}