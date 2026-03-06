package com.clansystem.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Clan {
    private UUID id;
    private String name;
    private UUID owner;
    private List<ClanMember> members = new ArrayList<>();
    private ClanHome home;
    private int totalKills;
    private int totalDeaths;
    private long totalPlaytime;
    private double totalMoney;
    private long createdAt;
    private boolean pvpEnabled = true;
    
    public ClanMember getMember(UUID uuid) {
        return members.stream()
            .filter(m -> m.getPlayerUUID().equals(uuid))
            .findFirst()
            .orElse(null);
    }
    
    public void removeMember(UUID uuid) {
        members.removeIf(m -> m.getPlayerUUID().equals(uuid));
    }
    
    public boolean isMember(UUID uuid) {
        return getMember(uuid) != null;
    }
    
    public boolean isOwner(UUID uuid) {
        ClanMember member = getMember(uuid);
        return member != null && member.getRank() == ClanRank.OWNER;
    }
    
    public boolean canManageMembers(UUID uuid) {
        ClanMember member = getMember(uuid);
        return member != null && member.getRank().canManageMembers();
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    public void addKill() {
        this.totalKills++;
    }
    
    public void addDeath() {
        this.totalDeaths++;
    }
    
    public void addPlaytime(long time) {
        this.totalPlaytime += time;
    }
}