package com.clansystem.storage;

import com.clansystem.ClanSystem;

import java.util.*;

public class PlayerDataStorage extends DataStorage {
    
    public PlayerDataStorage(ClanSystem plugin) {
        super(plugin, "playerdata.yml");
    }
    
    public void setPlayerClan(UUID playerUUID, UUID clanId) {
        if (clanId == null) {
            data.set("players." + playerUUID.toString() + ".clan", null);
        } else {
            data.set("players." + playerUUID.toString() + ".clan", clanId.toString());
        }
        save();
    }
    
    public UUID getPlayerClan(UUID playerUUID) {
        String clanIdString = data.getString("players." + playerUUID.toString() + ".clan");
        if (clanIdString == null) {
            return null;
        }
        try {
            return UUID.fromString(clanIdString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void addInvitation(UUID playerUUID, UUID clanId) {
        List<String> invitations = data.getStringList("players." + playerUUID.toString() + ".invitations");
        if (!invitations.contains(clanId.toString())) {
            invitations.add(clanId.toString());
            data.set("players." + playerUUID.toString() + ".invitations", invitations);
            save();
        }
    }
    
    public void removeInvitation(UUID playerUUID, UUID clanId) {
        List<String> invitations = data.getStringList("players." + playerUUID.toString() + ".invitations");
        invitations.remove(clanId.toString());
        data.set("players." + playerUUID.toString() + ".invitations", invitations);
        save();
    }
    
    public boolean hasInvitation(UUID playerUUID, UUID clanId) {
        List<String> invitations = data.getStringList("players." + playerUUID.toString() + ".invitations");
        return invitations.contains(clanId.toString());
    }
    
    public Set<UUID> getInvitations(UUID playerUUID) {
        List<String> invitations = data.getStringList("players." + playerUUID.toString() + ".invitations");
        Set<UUID> result = new HashSet<>();
        for (String idString : invitations) {
            try {
                result.add(UUID.fromString(idString));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }
}