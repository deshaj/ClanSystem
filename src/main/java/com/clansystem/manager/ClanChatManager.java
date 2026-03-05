package com.clansystem.manager;

import java.util.HashSet;
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
}