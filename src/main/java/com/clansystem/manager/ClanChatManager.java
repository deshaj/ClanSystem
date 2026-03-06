package com.clansystem.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClanChatManager {
    private final Set<UUID> clanChatEnabled = new HashSet<>();
    
    public void toggleClanChat(UUID playerUUID) {
        if (clanChatEnabled.contains(playerUUID)) {
            clanChatEnabled.remove(playerUUID);
        } else {
            clanChatEnabled.add(playerUUID);
        }
    }
    
    public boolean isClanChatEnabled(UUID playerUUID) {
        return clanChatEnabled.contains(playerUUID);
    }
    
    public void setClanChat(UUID playerUUID, boolean enabled) {
        if (enabled) {
            clanChatEnabled.add(playerUUID);
        } else {
            clanChatEnabled.remove(playerUUID);
        }
    }

    private final Set<UUID> creatingClan = new HashSet<>();
    private final Map<UUID, UUID> sendingJoinRequest = new HashMap<>();

    public void setCreatingClan(UUID playerUUID, boolean creating) {
        if (creating) {
            creatingClan.add(playerUUID);
        } else {
            creatingClan.remove(playerUUID);
        }
    }

    public boolean isCreatingClan(UUID playerUUID) {
        return creatingClan.contains(playerUUID);
    }

    public void setSendingJoinRequest(UUID playerUUID, UUID clanId) {
        if (clanId == null) {
            sendingJoinRequest.remove(playerUUID);
        } else {
            sendingJoinRequest.put(playerUUID, clanId);
        }
    }

    public UUID getSendingJoinRequest(UUID playerUUID) {
        return sendingJoinRequest.get(playerUUID);
    }

    public boolean isSendingJoinRequest(UUID playerUUID) {
        return sendingJoinRequest.containsKey(playerUUID);
    }
}